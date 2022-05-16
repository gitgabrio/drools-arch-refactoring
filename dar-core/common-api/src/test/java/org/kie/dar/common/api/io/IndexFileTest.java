package org.kie.dar.common.api.io;/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.jupiter.api.Test;
import org.kie.dar.common.api.exceptions.KieDARCommonException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class IndexFileTest {

    @Test
    void validatePathName() {
        String toValidate = "/this/is/valid/file.model_json";
        assertEquals(toValidate, IndexFile.validatePathName(toValidate));
    }

    @Test
    void validateURI() throws URISyntaxException {
        String basePath = "file:///this/is/valid/file.model_json";
        URI toValidate = new URI(basePath);
        assertEquals(toValidate, IndexFile.validateURI(toValidate));
    }

    @Test
    void validateWrongPathName() {
        String toValidate = "/this/is/invalid/file._json";
        try {
            IndexFile.validatePathName(toValidate);
            fail("Expecting KieDARCommonException");
        } catch (Exception e) {
            assertTrue(e instanceof KieDARCommonException);
            String expected = "Wrong file name file._json";
            assertEquals(expected, e.getMessage());
        }
        toValidate = "/this/is/invalid/file.model";
        try {
            IndexFile.validatePathName(toValidate);
            fail("Expecting KieDARCommonException");
        } catch (Exception e) {
            assertTrue(e instanceof KieDARCommonException);
            String expected = "Wrong file name file.model";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    void validateWrongURI() throws URISyntaxException {
        String basePath = "file:///this/is/invalid/file._json";
        URI toValidate = new URI(basePath);
        try {
            IndexFile.validateURI(toValidate);
            fail("Expecting KieDARCommonException");
        } catch (Exception e) {
            assertTrue(e instanceof KieDARCommonException);
            String expected = "Wrong file name file._json";
            assertEquals(expected, e.getMessage());
        }
        basePath = "file:///this/is/invalid/file.model";
        toValidate = new URI(basePath);
        try {
            IndexFile.validateURI(toValidate);
            fail("Expecting KieDARCommonException");
        } catch (Exception e) {
            assertTrue(e instanceof KieDARCommonException);
            String expected = "Wrong file name file.model";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    void getModel() {
        String fileName = "file_name.model_json";
        String expected = "model";
        String source = fileName;
        assertEquals(expected, IndexFile.getModel(source));
        source = File.separator + "dir" + File.separator + fileName;
        assertEquals(expected, IndexFile.getModel(source));
    }

    @Test
    void testGetModel() throws URISyntaxException {
        String fileName = "/this/is/valid/file.model_json";
        String expected = "model";
        IndexFile indexFile = new IndexFile(fileName);
        assertEquals(expected, indexFile.getModel());
        fileName = "file:///this/is/valid/file.model_json";
        URI uri = new URI(fileName);
        indexFile = new IndexFile(uri);
        assertEquals(expected, indexFile.getModel());
    }
}