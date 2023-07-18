package org.nuxeo.labs.download.link;

import static org.nuxeo.labs.download.link.helpers.TestHelper.FILE_CONTENT;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriterTest;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonWriter;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.download.link.enricher.PublicDownloadLinkEnricher;
import org.nuxeo.labs.download.link.helpers.TestHelper;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.IOException;


@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-public-download-link-core" })
public class TestPublicDownloadLinkEnricher extends AbstractJsonWriterTest.Local<DocumentModelJsonWriter, DocumentModel> {

    @Inject
    public CoreSession session;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;

    @Inject
    TestHelper th;

    public TestPublicDownloadLinkEnricher() {
        super(DocumentModelJsonWriter.class, DocumentModel.class);
    }

    @Test
    public void testEnricher() throws IOException {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT, null, null);
        JsonAssert json = jsonAssert(doc, RenderingContext.CtxBuilder.enrich("document", PublicDownloadLinkEnricher.NAME).get());
        json = json.has("contextParameters").isObject();
        json.properties(1);
        json.has(PublicDownloadLinkEnricher.NAME).isObject().has(FILE_CONTENT);
    }
}
