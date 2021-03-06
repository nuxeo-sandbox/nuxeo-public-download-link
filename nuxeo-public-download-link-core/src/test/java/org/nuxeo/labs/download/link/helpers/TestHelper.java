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

package org.nuxeo.labs.download.link.helpers;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;

import java.io.File;
import java.io.Serializable;

public class TestHelper {

    public static final String SAMPLE_FILE_PATH = "/files/document.docx";

    public DocumentModel getTestDocument(CoreSession session) {
        File file = new File(getClass().getResource(SAMPLE_FILE_PATH).getPath());
        Blob blob = new FileBlob(file);
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),"test","File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        return session.createDocument(doc);
    }

}
