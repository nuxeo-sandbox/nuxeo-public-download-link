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
package org.nuxeo.labs.download.link.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.io.download.PublicDownloadHelper;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.ecm.platform.ui.web.auth.interfaces.NuxeoAuthenticationPlugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.nuxeo.ecm.core.io.download.PublicDownloadHelper.PUBLIC_DOWNLOAD_TOKEN_PARAM;

public class PublicDownloadAuthenticator implements NuxeoAuthenticationPlugin {

    private static final Log log = LogFactory.getLog(PublicDownloadAuthenticator.class);

    public static final String PUBLIC_DOWNLOAD_PATH = "/nxfile/";

    @Override
    public Boolean handleLoginPrompt(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String baseURL) {
        return Boolean.FALSE;
    }

    @Override
    public UserIdentificationInfo handleRetrieveIdentity(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        URL url;
        try {
            url = new URL(httpRequest.getRequestURL().toString());
        } catch (MalformedURLException e) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        String fullPath = url.getPath();

        String path = fullPath.substring(fullPath.indexOf(PUBLIC_DOWNLOAD_PATH)+PUBLIC_DOWNLOAD_PATH.length());

        String token = httpRequest.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM);
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        if (PublicDownloadHelper.isValidPublicDownloadURL(path,token)) {
            return new UserIdentificationInfo("transient/"+token);
        } else {
            return null;
        }
    }

    @Override
    public Boolean needLoginPrompt(HttpServletRequest httpRequest) {
        return Boolean.FALSE;
    }

    @Override
    public void initPlugin(Map<String, String> parameters) {
        //do nothing
    }

    @Override
    public List<String> getUnAuthenticatedURLPrefix() {
        return null;
    }
}