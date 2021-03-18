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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.download.link.helpers.TestHelper;
import org.nuxeo.labs.download.link.service.PublicDownloadLinkService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.labs.download.link.helpers.TestHelper.FILES_FILES;
import static org.nuxeo.labs.download.link.helpers.TestHelper.FILE_CONTENT;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-public-download-link-core"
})
public class TestPublicDownloadLinkService {

    @Inject
    public CoreSession session;

    @Inject
    PublicDownloadLinkService publicDownloadLinkService;

    @Inject
    TestHelper th;

    @Test
    public void testhasNoPermission() {
        DocumentModel doc = th.getTestDocument(session);
        assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
    }

    @Test
    public void testhasPermission() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        assertTrue(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
    }

    @Test
    public void testhasPermissionOnDifferentFile() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILES_FILES));
    }

    @Test
    public void testSetPermission() {
        DocumentModel doc = th.getTestDocument(session);
        String token = publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        assertTrue(StringUtils.isNotEmpty(token));
        assertTrue(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
    }

    @Test
    public void testSetPermissionTwice() {
        DocumentModel doc = th.getTestDocument(session);
        String token = publicDownloadLinkService.setPublicDownloadPermission(doc, FILE_CONTENT);
        String token2 = publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        assertEquals(token2,token);
    }

    @Test
    public void testSetPermissionMultipleXpath() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILES_FILES);
        assertTrue(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
        assertTrue(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILES_FILES));
    }

    @Test
    public void testRemovePermission() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        assertTrue(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
        publicDownloadLinkService.removePublicDownloadPermission(doc,FILE_CONTENT);
        assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
    }

    @Test
    public void testRemoveOnePermission() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILES_FILES);
        publicDownloadLinkService.removePublicDownloadPermission(doc,FILE_CONTENT);
        assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
        assertTrue(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILES_FILES));
    }

    @Test
    public void testRemoveAllPermissions() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILES_FILES);
        publicDownloadLinkService.removePublicDownloadPermissions(doc);
        assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILE_CONTENT));
        assertFalse(publicDownloadLinkService.hasPublicDownloadPermission(doc,FILES_FILES));
    }

    @Test
    public void testInvalidTokenWhenNoPermission() {
        DocumentModel doc = th.getTestDocument(session);
        assertFalse(publicDownloadLinkService.isValidToken(doc, FILE_CONTENT,"abc"));
    }

    @Test
    public void testInvalidToken() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        assertFalse(publicDownloadLinkService.isValidToken(doc,FILE_CONTENT,"abc"));
    }

    @Test
    public void testGetDownloadLinkWhenPermission() {
        DocumentModel doc = th.getTestDocument(session);
        publicDownloadLinkService.setPublicDownloadPermission(doc,FILE_CONTENT);
        String link = publicDownloadLinkService.getPublicDownloadLink(doc,FILE_CONTENT);
        assertTrue(StringUtils.isNotEmpty(link));
    }

    @Test
    public void testGetDownloadLinkWhenNoPermission() {
        DocumentModel doc = th.getTestDocument(session);
        String link = publicDownloadLinkService.getPublicDownloadLink(doc,FILE_CONTENT);
        assertNull(link);
    }

}
