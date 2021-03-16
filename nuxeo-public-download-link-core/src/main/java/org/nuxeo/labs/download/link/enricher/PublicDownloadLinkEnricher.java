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

package org.nuxeo.labs.download.link.enricher;

import com.fasterxml.jackson.core.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.util.Map;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

@Setup(mode = SINGLETON, priority = REFERENCE)
public class PublicDownloadLinkEnricher extends AbstractJsonEnricher<DocumentModel> {

public static final String NAME = "publicDownload";

    public PublicDownloadLinkEnricher() {
        super(NAME);
    }

    @Override
    public void write(JsonGenerator jg, DocumentModel enriched) throws IOException {
        PublicDownloadLinkService publicDownloadLinkService = Framework.getService(PublicDownloadLinkService.class);
        Map<String,String> links = publicDownloadLinkService.getAllPublicDownloadLink(enriched);
        jg.writeFieldName(NAME);
        jg.writeStartObject();
        for(Map.Entry<String,String> entry : links.entrySet()) {
            jg.writeStringField(entry.getKey(),entry.getValue());
        }
        jg.writeEndObject();
    }
}
