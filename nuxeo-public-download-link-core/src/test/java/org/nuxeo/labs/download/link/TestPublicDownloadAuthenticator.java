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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.io.download.PublicDownloadHelper;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainerFeature;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

@RunWith(FeaturesRunner.class)
@Features({RestServerFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
    "org.nuxeo.ecm.platform.web.common",
    "nuxeo-public-download-link-core"
})
public class TestPublicDownloadAuthenticator {

    public static final String HOST = "http://localhost";
    public static final String FILE_CONTENT = "file:content";

    @Inject
    public ServletContainerFeature servletContainerFeature;

    @Inject
    public CoreSession session;

    @Inject
    public DownloadService downloadService;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;

    @Inject
    protected TransactionalFeature transactionalFeature;

    public DocumentModel doc;
    public String baseUrl;
    public String token;

    @Before
    public void setup() {
        baseUrl = HOST + ":" + servletContainerFeature.getPort() + "/";
        Framework.getProperties().put("nuxeo.url",baseUrl);
        File file = new File(getClass().getResource("/files/document.docx").getPath());
        Blob blob = new FileBlob(file);
        doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),"test","File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);
        token = publicDownloadLinkService.setPublicDownloadPermission(doc);
        doc = session.saveDocument(doc);
        transactionalFeature.nextTransaction();
    }

    @Test
    public void testWithToken() throws IOException {
        String url = publicDownloadLinkService.getPublicDownloadLink(doc,FILE_CONTENT);
        System.out.println(url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithWrongToken() throws IOException {
        String url = downloadService.getFullDownloadUrl(doc, FILE_CONTENT, (Blob) doc.getPropertyValue(FILE_CONTENT),
                baseUrl) + "&" + PublicDownloadHelper.PUBLIC_DOWNLOAD_TOKEN_PARAM +"=456";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithoutToken() throws IOException {
        String url = downloadService.getFullDownloadUrl(doc, FILE_CONTENT, (Blob) doc.getPropertyValue(FILE_CONTENT), baseUrl);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
    }

}
