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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.CreateInvalidationRequest;
import com.amazonaws.services.cloudfront.model.InvalidationBatch;
import com.amazonaws.services.cloudfront.model.Paths;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkBuilder;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.aws.NuxeoAWSCredentialsProvider;

import java.net.URI;
import java.util.UUID;

import static org.nuxeo.ecm.core.io.download.DownloadService.NXFILE;
import static org.nuxeo.labs.download.link.service.PublicDownloadLinkServiceImpl.PUBLIC_DOWNLOAD_PATH;

public class CloudfrontPublicDownloadLinkBuilder implements PublicDownloadLinkBuilder {

    public static final String CLOUDFRONT_DISTRIB_NAME_PROPERTY = "nuxeo.s3storage.cloudfront.distribDomain";
    public static final String CLOUDFRONT_DISTRIB_ID_PROPERTY = "nuxeo.s3storage.cloudfront.distribId";
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
        String distributionId = Framework.getProperty(CLOUDFRONT_DISTRIB_ID_PROPERTY);
        AWSCredentialsProvider awsCredentialsProvider = NuxeoAWSCredentialsProvider.getInstance();
        AmazonCloudFrontClient client =  (AmazonCloudFrontClient) AmazonCloudFrontClientBuilder.standard()
                .withCredentials(awsCredentialsProvider).build();
        URI uri = URI.create(link);
        Paths invalidation_paths = new Paths().withItems(uri.getPath()).withQuantity(1);
        InvalidationBatch invalidation_batch = new InvalidationBatch(invalidation_paths, UUID.randomUUID().toString());
        CreateInvalidationRequest invalidation = new CreateInvalidationRequest(distributionId, invalidation_batch);
        client.createInvalidation(invalidation);
    }
}
