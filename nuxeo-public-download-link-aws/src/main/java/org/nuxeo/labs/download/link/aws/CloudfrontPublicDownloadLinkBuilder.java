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

package org.nuxeo.labs.download.link.aws;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkBuilder;
import org.nuxeo.runtime.api.Framework;

import static org.nuxeo.ecm.core.io.download.DownloadService.NXFILE;
import static org.nuxeo.labs.download.link.service.PublicDownloadLinkServiceImpl.PUBLIC_DOWNLOAD_PATH;

public class CloudfrontPublicDownloadLinkBuilder implements PublicDownloadLinkBuilder {

    public static final String CLOUDFRONT_DISTRIB_NAME_PROPERTY = "nuxeo.s3storage.cloudfront.distribDomain";
    public static final String CLOUDFRONT_PREFIX = "public";

    @Override
    public String getPublicDownloadLink(DocumentModel doc, String xpath, String token) {
        String distributionName = Framework.getProperty(CLOUDFRONT_DISTRIB_NAME_PROPERTY);
        DownloadService downloadService = Framework.getService(DownloadService.class);
        Blob blob = downloadService.resolveBlob(doc,xpath);
        String downloadPath = downloadService.getDownloadUrl(doc, xpath, blob.getFilename()).replace(NXFILE,PUBLIC_DOWNLOAD_PATH);
        return String.format("https://%s/%s/%s/%s",distributionName,CLOUDFRONT_PREFIX,token,downloadPath);
    }

    @Override
    public void publicDownloadPermissionRevoked(DocumentModel doc, String xpath, String link) {
        //todo send invalidation request to cloudfront
    }
}
