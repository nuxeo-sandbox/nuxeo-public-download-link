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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.io.download.PublicDownloadHelper;
import org.nuxeo.runtime.api.Framework;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.nuxeo.ecm.core.api.NuxeoPrincipal.TRANSIENT_USER_PREFIX;

public class PublicDownloadLinkServiceImpl implements PublicDownloadLinkService {

    public static final String NUXEO_URL_KEY = "nuxeo.url";
    public static final String PUBLIC_DOWNLOAD_ACL_PREFIX = "public_download/";

    @Override
    public String setPublicDownloadPermission(DocumentModel doc) {
        CoreSession session = doc.getCoreSession();
        ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();
        String token = UUID.randomUUID().toString();
        ACE ace = ACE.builder(TRANSIENT_USER_PREFIX + token, SecurityConstants.READ).creator(session.getPrincipal().getName()).build();
        acp.addACE(PUBLIC_DOWNLOAD_ACL_PREFIX + token, ace);
        doc.setACP(acp, true);
        return token;
    }

    @Override
    public void removePublicDownloadPermission(DocumentModel doc) {
        //todo
    }

    @Override
    public String getPublicDownloadLink(DocumentModel doc, String xpath) {
        ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();
        Optional<ACL> optional = Arrays.stream(acp.getACLs())
                .filter(acl -> acl.getName().startsWith(PUBLIC_DOWNLOAD_ACL_PREFIX))
                .findFirst();
        if (optional.isEmpty()) {
            return null;
        } else {
            String token = optional.get().getName().substring(PUBLIC_DOWNLOAD_ACL_PREFIX.length());
            String baseUrl = Framework.getProperty(NUXEO_URL_KEY);
            DownloadService downloadService = Framework.getService(DownloadService.class);
            Blob blob = downloadService.resolveBlob(doc,xpath);
            return downloadService.getFullDownloadUrl(doc, xpath, blob, baseUrl)+
                    "&" + PublicDownloadHelper.PUBLIC_DOWNLOAD_TOKEN_PARAM +"="+ token;
        }
    }

    @Override
    public boolean isValidToken(DocumentModel doc, String token) {
        ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();
        Optional<ACL> optional = Arrays.stream(acp.getACLs())
                .filter(acl -> acl.getName().equals(PUBLIC_DOWNLOAD_ACL_PREFIX+token))
                .findFirst();
        return optional.isPresent();
    }
}