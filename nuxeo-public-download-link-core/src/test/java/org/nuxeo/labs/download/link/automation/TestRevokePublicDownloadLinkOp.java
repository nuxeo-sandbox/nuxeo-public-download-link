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

package org.nuxeo.labs.download.link.automation;

import static org.nuxeo.labs.download.link.helpers.TestHelper.FILES_FILES;
import static org.nuxeo.labs.download.link.helpers.TestHelper.FILE_CONTENT;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.labs.download.link.helpers.TestHelper;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-public-download-link-core" })
public class TestRevokePublicDownloadLinkOp {

    @Inject
    CoreSession session;

    @Inject
    AutomationService as;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;

    @Inject
    TestHelper th;

    @Test
    public void testRevokeDownloadLink() throws OperationException {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT, null, null);
        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestRevokeDownloadLinkOp");
        chain.add(RevokePublicDownloadLinkOp.ID);
        doc = (DocumentModel) as.run(ctx, chain);
        Assert.assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc, FILE_CONTENT));
    }

    @Test
    public void testRevokeDownloadLinkWithXpath() throws OperationException {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc, FILES_FILES, null, null);
        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestRevokeDownloadLinkOp");
        chain.add(RevokePublicDownloadLinkOp.ID).set("xpath", FILES_FILES);
        doc = (DocumentModel) as.run(ctx, chain);
        Assert.assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc, FILES_FILES));
    }

    @Test
    public void testRevokeAllDownloadLink() throws OperationException {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT, null, null);
        publicDownloadLinkService.setPublicDownloadPermission(doc, FILES_FILES, null, null);
        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestRevokeDownloadLinkOp");
        chain.add(RevokePublicDownloadLinkOp.ID).set("all", true);
        doc = (DocumentModel) as.run(ctx, chain);
        Assert.assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc, FILE_CONTENT));
        Assert.assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc, FILES_FILES));
    }

}
