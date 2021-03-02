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
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;

import java.util.Calendar;

@Operation(
        id = CreatePublicDownloadLinkOp.ID,
        category = Constants.CAT_DOCUMENT,
        label = "DocuSign: Get Signed Blobs",
        description = "Get the signed blobs for the given envelope ID")
public class CreatePublicDownloadLinkOp {

    public static final String ID = "DSGetSignedBlobsOp";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected PublicDownloadLinkService publicDownloadLinkService;

    @Param(name = "begin", required = false, description = "ACE begin date.")

    protected Calendar begin;
    @Param(name = "end", required = false, description = "ACE end date.")
    protected Calendar end;

    @OperationMethod
    public Blob run(DocumentModel doc)  {
        return null;
    }

}
