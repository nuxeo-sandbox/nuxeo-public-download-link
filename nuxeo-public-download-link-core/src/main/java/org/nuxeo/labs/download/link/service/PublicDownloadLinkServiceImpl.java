/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.labs.download.link.service;

import static org.nuxeo.ecm.core.api.NuxeoPrincipal.TRANSIENT_USER_PREFIX;
import static org.nuxeo.ecm.core.io.download.DownloadService.NXFILE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

public class PublicDownloadLinkServiceImpl extends DefaultComponent implements PublicDownloadLinkService {

    public static final String LINK_BUILDER_EP = "builder";

    public static final String PUBLIC_DOWNLOAD_PATH = "nxpublicfile";

    public static final String PUBLIC_DOWNLOAD_TOKEN_PARAM = "download_token";

    public static final String NUXEO_URL_KEY = "nuxeo.url";

    public static final String PUBLIC_DOWNLOAD_ACL_PREFIX = "public_download/";

    public PublicDownloadLinkBuilder linkBuilder = null;

    @Override
    public void registerContribution(Object contribution, String xp, ComponentInstance component) {
        if (LINK_BUILDER_EP.equals(xp)) {
            linkBuilder = ((PublicDownloadLinkBuilderDescriptor) contribution).newInstance();
        }
        super.registerContribution(contribution, xp, component);
    }

    @Override
    public boolean hasPublicDownloadPermission(DocumentModel doc, String xpath) {

        return getExistingDownloadPermission(doc, xpath).length > 0;
    }

    @Override
    public boolean hasEffectivePublicDownloadPermission(DocumentModel doc, String xpath) {

        ACL[] acls = getExistingDownloadPermission(doc, xpath);

        return filterACLsForBeginEnd(acls).length > 0;
    }

    @Override
    public String setPublicDownloadPermission(DocumentModel doc, String xpath) {
        return this.setPublicDownloadPermission(doc, xpath, null, null);
    }

    @Override
    public String setPublicDownloadPermission(DocumentModel doc, String xpath, Calendar begin, Calendar end) {
        ACL[] acls = getExistingDownloadPermission(doc, xpath);
        if (acls.length <= 0) {
            CoreSession session = doc.getCoreSession();
            ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();
            String token = UUID.randomUUID().toString();
            ACE ace = ACE.builder(TRANSIENT_USER_PREFIX + token, SecurityConstants.READ)
                         .creator(session.getPrincipal().getName())
                         .begin(begin)
                         .end(end)
                         .build();
            acp.addACE(getACLPrefix(xpath) + token, ace);
            doc.setACP(acp, true);
            return token;
        } else {
            return acls[0].getName().substring(getACLPrefix(xpath).length());
        }
    }

    @Override
    public void removePublicDownloadPermission(DocumentModel doc, String xpath) {
        String link = getPublicDownloadLink(doc, xpath);
        ACL[] acls = getExistingDownloadPermission(doc, xpath);
        ACP acp = doc.getACP();
        for (ACL acl : acls) {
            acp.removeACL(acl.getName());
        }
        doc.setACP(acp, true);
        if (linkBuilder != null) {
            linkBuilder.publicDownloadPermissionRevoked(doc, xpath, link);
        }
    }

    @Override
    public void removePublicDownloadPermissions(DocumentModel doc) {
        ACL[] acls = getExistingDownloadPermissions(doc);
        ACP acp = doc.getACP();
        for (ACL acl : acls) {
            acp.removeACL(acl.getName());
        }
        doc.setACP(acp, true);
    }

    @Override
    public String getPublicDownloadLink(DocumentModel doc, String xpath) {
        ACL[] acls = getExistingDownloadPermission(doc, xpath);
        if (acls.length == 0) {
            return null;
        }
        String token = acls[0].getName().substring(getACLPrefix(xpath).length());

        if (linkBuilder != null) {
            return linkBuilder.getPublicDownloadLink(doc, xpath, token);
        } else {
            DownloadService downloadService = Framework.getService(DownloadService.class);
            Blob blob = downloadService.resolveBlob(doc, xpath);
            String downloadPath = downloadService.getDownloadUrl(doc, xpath, blob.getFilename())
                                                 .replace(NXFILE, PUBLIC_DOWNLOAD_PATH);
            String baseUrl = Framework.getProperty(NUXEO_URL_KEY);
            return String.format("%s/%s&%s=%s", baseUrl, downloadPath, PUBLIC_DOWNLOAD_TOKEN_PARAM, token);
        }
    }

    @Override
    public Map<String, String> getAllPublicDownloadLink(DocumentModel doc) {
        ACL[] acls = getExistingDownloadPermissions(doc);
        Map<String, String> links = new HashMap<>();
        for (ACL acl : acls) {
            String name = acl.getName();
            String xpath = name.substring(PUBLIC_DOWNLOAD_ACL_PREFIX.length(), name.lastIndexOf('/'));
            links.put(xpath, getPublicDownloadLink(doc, xpath));
        }
        return links;
    }

    @Override
    public boolean isValidToken(DocumentModel doc, String xpath, String token) {
        Optional<ACL> optional = Arrays.stream(getExistingDownloadPermission(doc, xpath))
                                       .filter(acl -> acl.getName().equals(getACLPrefix(xpath) + token))
                                       .findFirst();
        return optional.isPresent();
    }

    protected ACL[] filterACLsForBeginEnd(ACL[] acls) {

        ArrayList<ACL> finalACLs = new ArrayList<ACL>();
        if (acls != null) {
            for (ACL acl : acls) {
                for (ACE ace : acl.getACEs()) {
                    if (ace.isEffective()) {
                        finalACLs.add(acl);
                    }
                }
            }
        }

        return (ACL[]) finalACLs.stream().toArray(ACL[]::new);

    }

    public ACL[] getExistingDownloadPermissions(DocumentModel doc) {
        ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();

        ACL[] acls = Arrays.stream(acp.getACLs())
                           .filter(acl -> acl.getName().startsWith(PUBLIC_DOWNLOAD_ACL_PREFIX))
                           .toArray(ACL[]::new);

        return acls;
    }

    public ACL[] getExistingDownloadPermission(DocumentModel doc, String xpath) {
        ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();
        ACL[] acls = Arrays.stream(acp.getACLs())
                           .filter(acl -> acl.getName().startsWith(getACLPrefix(xpath)))
                           .toArray(ACL[]::new);

        return acls;
    }

    public String getACLPrefix(String xpath) {
        return PUBLIC_DOWNLOAD_ACL_PREFIX + xpath + "/";
    }

}