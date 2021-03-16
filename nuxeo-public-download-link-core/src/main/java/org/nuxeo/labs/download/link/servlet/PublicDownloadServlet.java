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

package org.nuxeo.labs.download.link.servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.io.download.PublicDownloadHelper;
import org.nuxeo.ecm.platform.ui.web.download.DownloadServlet;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.login.NuxeoLoginContext;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.nuxeo.ecm.core.api.NuxeoPrincipal.TRANSIENT_USER_PREFIX;
import static org.nuxeo.ecm.core.io.download.DownloadService.NXFILE;
import static org.nuxeo.labs.download.link.service.PublicDownloadLinkServiceImpl.PUBLIC_DOWNLOAD_PATH;
import static org.nuxeo.labs.download.link.service.PublicDownloadLinkServiceImpl.PUBLIC_DOWNLOAD_TOKEN_PARAM;

public class PublicDownloadServlet extends DownloadServlet {

    private static final Log log = LogFactory.getLog(PublicDownloadServlet.class);

    protected void handleDownload(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        URL url;
        try {
            url = new URL(httpRequest.getRequestURL().toString());
        } catch (MalformedURLException e) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String baseUrl = VirtualHostHelper.getBaseURL(httpRequest);

        String fullPath = url.getPath();

        String path = fullPath.substring(fullPath.indexOf(PUBLIC_DOWNLOAD_PATH)+PUBLIC_DOWNLOAD_PATH.length()+1);

        String token = httpRequest.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM);

        if (StringUtils.isEmpty(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (NuxeoLoginContext loginContext = Framework.loginUser(TRANSIENT_USER_PREFIX+token)) {
            boolean isValid = TransactionHelper.runInTransaction(() -> PublicDownloadHelper.isValidPublicDownloadRequest(path,token));
            if (isValid) {
                DownloadService downloadService = Framework.getService(DownloadService.class);
                downloadService.handleDownload(httpRequest, httpResponse, baseUrl, NXFILE+"/"+path);
            } else {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (LoginException | DocumentSecurityException e) {
            log.warn(e);
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
