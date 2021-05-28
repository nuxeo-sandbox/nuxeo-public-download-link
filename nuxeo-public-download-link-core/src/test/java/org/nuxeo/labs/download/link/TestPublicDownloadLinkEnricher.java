package org.nuxeo.labs.download.link;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.download.link.enricher.PublicDownloadLinkEnricher;
import org.nuxeo.labs.download.link.helpers.TestHelper;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;

import java.io.StringWriter;

import static org.nuxeo.labs.download.link.helpers.TestHelper.FILE_CONTENT;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-public-download-link-core"
})
public class TestPublicDownloadLinkEnricher {

    @Inject
    public CoreSession session;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;

    @Inject
    TestHelper th;

    @Test
    public void testEnricher() throws Exception {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT, null, null);
        JsonFactory factory = new JsonFactory();
        StringWriter jsonObjectWriter = new StringWriter();
        JsonGenerator generator = factory.createGenerator(jsonObjectWriter);
        generator.writeStartObject();
        new PublicDownloadLinkEnricher().write(generator,doc);
        generator.writeEndObject();
        generator.close();
        String json = jsonObjectWriter.toString();
        JSONObject object = new JSONObject(json);
        JSONObject enricherObject = object.getJSONObject(PublicDownloadLinkEnricher.NAME);
        String url = enricherObject.getString(FILE_CONTENT);
        Assert.assertNotNull(url);
    }
}
