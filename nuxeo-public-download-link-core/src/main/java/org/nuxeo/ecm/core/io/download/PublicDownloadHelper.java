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

package org.nuxeo.ecm.core.io.download;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.api.Framework;

/**
 * Using a class in the same package as the download service in order to access protected attributes in DownloadBlobInfo
 */
public class PublicDownloadHelper {

    public static boolean isValidPublicDownloadRequest(String path, String token) {
        DownloadBlobInfo downloadBlobInfo = new DownloadBlobInfo(path);
        CoreSession session = CoreInstance.getCoreSession(downloadBlobInfo.repository);
        DocumentModel doc = session.getDocument(new IdRef(downloadBlobInfo.docId));
        String xpath = downloadBlobInfo.xpath;
        PublicDownloadLinkService publicDownloadLinkService = Framework.getService(PublicDownloadLinkService.class);
        return publicDownloadLinkService.isValidToken(doc, xpath, token);
    }
}
