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

import java.util.Calendar;
import java.util.Map;

public interface PublicDownloadLinkService {

    /**
     * Returns true if there is a permission for the given path.
     * WARNING: Only checks for the existence of a permission, it does not check it is valid? An invalid permission
     * would be a permission with start and/or end date, incompatible with the current date => in this case, the API
     * still returns true, "there is a permission"
     * 
     * @param doc
     * @param xpath
     * @return true if there is a permission for the given xpath
     */
    public boolean hasPublicDownloadPermission(DocumentModel doc, String xpath);

    /**
     * Returns true if there is a permission for the given path and it is valid in terms of begin/end date (if any)
     * 
     * @param doc
     * @param xpath
     * @return true if there is a permission for the given xpath and it's start/end date is OK with the curret date
     */
    public boolean hasEffectivePublicDownloadPermission(DocumentModel doc, String xpath);

    /**
     * Set an ACL to allow public download for the given xpath
     * 
     * @param doc
     * @param xpath
     * @param begin (can be null)
     * @param end (can be null)
     * @return the token
     */
    public String setPublicDownloadPermission(DocumentModel doc, String xpath, Calendar begin, Calendar end);

    /**
     * Remove existing ACL to allow public download for the given xpath
     * 
     * @param doc
     * @param xpath
     */
    public void removePublicDownloadPermission(DocumentModel doc, String xpath);

    /**
     * Remove all existing ACL to allow public download
     * 
     * @param doc
     */
    public void removePublicDownloadPermissions(DocumentModel doc);

    /**
     * Get the public download link
     * 
     * @param doc the document
     * @param xpath the blob xpath
     * @return a url string
     */
    public String getPublicDownloadLink(DocumentModel doc, String xpath);

    /**
     * Get all the existing public download link for the given document
     * 
     * @param doc the document
     * @return a map where the key is the xpath and the value the download link
     */
    public Map<String, String> getAllPublicDownloadLink(DocumentModel doc);

    /**
     * @param doc
     * @param xpath
     * @param token
     * @return true is the token is valid
     */
    public boolean isValidToken(DocumentModel doc, String xpath, String token);

}
