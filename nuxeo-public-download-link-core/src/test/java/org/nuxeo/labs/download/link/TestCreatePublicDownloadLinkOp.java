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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.labs.download.link.automation.CreatePublicDownloadLinkOp;
import org.nuxeo.labs.download.link.helpers.TestHelper;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-public-download-link-core"
})
public class TestCreatePublicDownloadLinkOp {

    @Inject
    CoreSession session;

    @Inject
    AutomationService as;

    @Inject
    TestHelper th;

    @Test
    public void testGetDownloadLink() throws Exception {
        OperationContext ctx = new OperationContext();
        ctx.setInput(th.getTestDocument(session));
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestGetDownloadLinkOp");
        chain.add(CreatePublicDownloadLinkOp.ID);
        Blob blob = (Blob) as.run(ctx, chain);
        Assert.assertNotNull(blob);
    }

}
