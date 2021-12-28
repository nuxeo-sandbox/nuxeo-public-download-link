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

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;

@Operation(id = RevokePublicDownloadLinkOp.ID, category = Constants.CAT_DOCUMENT, label = "Revoke a public download url", description = "Revoke a public download url")
public class RevokePublicDownloadLinkOp {

    public static final String ID = "RevokePublicDownloadLink";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected PublicDownloadLinkService publicDownloadLinkService;

    @Param(name = "xpath", required = false, description = "File xpath")
    protected String xpath = "file:content";

    @Param(name = "all", required = false, description = "Set to true to revoke all links")
    protected boolean all = false;

    @Param(name = "save", required = false, description = "Set to true to save the document")
    protected boolean save = true;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) {
        boolean changed = false;
        if (all) {
            publicDownloadLinkService.removePublicDownloadPermissions(doc);
            changed = true;
        } else if (publicDownloadLinkService.hasPublicDownloadPermission(doc, xpath)) {
            publicDownloadLinkService.removePublicDownloadPermission(doc, xpath);
            changed = true;
        }
        if (changed && save) {
            session.saveDocument(doc);
        }
        return doc;
    }

}
