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

package org.nuxeo.labs.download.link;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.nuxeo.ecm.core.io.download.DownloadService.NXFILE;
import static org.nuxeo.labs.download.link.helpers.TestHelper.FILES_FILES;
import static org.nuxeo.labs.download.link.helpers.TestHelper.FILE_CONTENT;
import static org.nuxeo.labs.download.link.service.PublicDownloadLinkServiceImpl.PUBLIC_DOWNLOAD_PATH;
import static org.nuxeo.labs.download.link.service.PublicDownloadLinkServiceImpl.PUBLIC_DOWNLOAD_TOKEN_PARAM;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.download.link.helpers.TestHelper;
import org.nuxeo.labs.download.link.mock.MockHttpServletResponse;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.labs.download.link.servlet.PublicDownloadServlet;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, TransactionalFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-public-download-link-core" })
public class TestPublicDownloadServlet {

    public static final String HOST = "http://localhost";

    @Inject
    public CoreSession session;

    @Inject
    public DownloadService downloadService;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Inject
    TestHelper th;

    public DocumentModel doc;

    public String token;

    @Before
    public void setup() {
        Framework.getProperties().put("nuxeo.url", HOST);
        doc = th.getTestDocument(session);
        token = publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT, null, null);
        doc = session.saveDocument(doc);
        transactionalFeature.nextTransaction();
    }

    @Test
    public void testWithToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        String url = publicDownloadLinkService.getPublicDownloadLink(doc, FILE_CONTENT);

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn(token);

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testWithWrongToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        String url = downloadService.getFullDownloadUrl(doc, FILE_CONTENT, (Blob) doc.getPropertyValue(FILE_CONTENT),
                HOST + "/").replace(NXFILE, PUBLIC_DOWNLOAD_PATH);

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn("456");

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testWithoutToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        String url = downloadService.getFullDownloadUrl(doc, FILE_CONTENT, (Blob) doc.getPropertyValue(FILE_CONTENT),
                HOST + "/").replace(NXFILE, PUBLIC_DOWNLOAD_PATH);

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn(null);

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testWithTokenAndWrongXpath() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        String url = publicDownloadLinkService.getPublicDownloadLink(doc, FILE_CONTENT)
                                              .replace(FILE_CONTENT, FILES_FILES);

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn(token);

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testWithValidTokenAndWrongDocument() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        DocumentModel substituteDoc = th.getTestDocument(session);

        String url = publicDownloadLinkService.getPublicDownloadLink(doc, FILE_CONTENT)
                                              .replace(doc.getId(), substituteDoc.getId());

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn(token);

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testWithTokenAndValidDates() throws IOException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        String url = publicDownloadLinkService.getPublicDownloadLink(doc, FILE_CONTENT);

        // Now that we have the link, change the permission
        publicDownloadLinkService.removePublicDownloadPermissions(doc);
        Calendar begin = new GregorianCalendar();
        begin.add(Calendar.DAY_OF_MONTH, -1);
        Calendar end = new GregorianCalendar();
        end.add(Calendar.DAY_OF_MONTH, 1);
        token = publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT, begin, end);

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn(token);

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testWithTokenAndInvalidDates() throws IOException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = new MockHttpServletResponse();

        String url = publicDownloadLinkService.getPublicDownloadLink(doc, FILE_CONTENT);

        // Now that we have the link, change the permission
        publicDownloadLinkService.removePublicDownloadPermissions(doc);
        Calendar begin = new GregorianCalendar();
        begin.add(Calendar.DAY_OF_MONTH, 10);
        token = publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT, begin, null);

        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getParameter(PUBLIC_DOWNLOAD_TOKEN_PARAM)).thenReturn(token);

        new PublicDownloadServlet().doGet(request, response);

        assertEquals(404, response.getStatus());
    }

}
