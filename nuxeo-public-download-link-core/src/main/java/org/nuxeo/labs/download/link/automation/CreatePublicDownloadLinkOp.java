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

import java.util.Calendar;

import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;

@Operation(id = CreatePublicDownloadLinkOp.ID, category = Constants.CAT_DOCUMENT, label = "Get a public download url", description = "Get a public download url")
public class CreatePublicDownloadLinkOp {

    public static final String ID = "CreatePublicDownloadLink";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected PublicDownloadLinkService publicDownloadLinkService;

    @Param(name = "xpath", required = false, description = "File xpath")
    protected String xpath = "file:content";

    @Param(name = "begin", required = false, description = "Public link start date")
    protected Calendar begin;

    @Param(name = "end", required = false, description = "Public link end date")
    protected Calendar end;

    @Param(name = "replace", required = false, description = "Replace an existing eprmission, if any")
    protected boolean replace = false;

    @Param(name = "save", required = false, description = "Set to true to save the document")
    protected boolean save = true;

    @OperationMethod
    public Blob run(DocumentModel doc) {

        boolean hasPermission = publicDownloadLinkService.hasPublicDownloadPermission(doc, xpath);

        boolean addPermission = true;
        if (hasPermission) {
            if (replace) {
                publicDownloadLinkService.removePublicDownloadPermission(doc, xpath);
            } else {
                addPermission = false;
            }
        }

        if (addPermission) {
            publicDownloadLinkService.setPublicDownloadPermission(doc, xpath, begin, end);
            if (save) {
                session.saveDocument(doc);
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(xpath, publicDownloadLinkService.getPublicDownloadLink(doc, xpath));

        return new StringBlob(jsonObject.toString(), "application/json");
    }

}
