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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.labs.download.link.aws.CloudfrontPublicDownloadLinkBuilder.CLOUDFRONT_DISTRIB_NAME_PROPERTY;
import static org.nuxeo.labs.download.link.aws.CloudfrontPublicDownloadLinkBuilder.CLOUDFRONT_PREFIX;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-public-download-link-core",
        "nuxeo-public-download-link-aws",
        "nuxeo-public-download-link-aws:builder-test-contrib.xml"
})
public class TestCloudfrontPublicDownloadLinkBuilder {

    @Inject
    public CoreSession session;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;


    @Test
    public void testGetDownloadLinkWithCloudfront() throws MalformedURLException {
        String distributionName = "my.cloudfront.net";
        Framework.getProperties().put(CLOUDFRONT_DISTRIB_NAME_PROPERTY, distributionName);

        File file = new File(getClass().getResource("/files/document.docx").getPath());
        Blob blob = new FileBlob(file);
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "test", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        String token = publicDownloadLinkService.setPublicDownloadPermission(doc, "file:content");
        String link = publicDownloadLinkService.getPublicDownloadLink(doc, "file:content");

        assertNotNull(link);
        URL url = new URL(link);
        assertEquals(distributionName, url.getAuthority());
        String path = url.getPath();
        String[] segment = path.split("/");
        assertEquals(CLOUDFRONT_PREFIX, segment[1]);
        assertEquals(token, segment[2]);
    }

}
