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

import org.nuxeo.ecm.core.api.DocumentModel;

public interface PublicDownloadLinkService {

    /**
     * Set an ACL to allow public download
     * @param doc
     * @return the ACL name
     */
    public String setPublicDownloadPermission(DocumentModel doc);

    /**
     * Remove existing ACL to allow public download
     * @param doc
     */
    public void removePublicDownloadPermission(DocumentModel doc);

    /**
     * Get the public download link
     * @param doc the document
     * @param xpath the blob xpath
     * @return a url string
     */
    public String getPublicDownloadLink(DocumentModel doc, String xpath);


    /**
     *
     * @param doc
     * @param token
     * @return true is the token is valid
     */
    public boolean isValidToken(DocumentModel doc, String token);

}
