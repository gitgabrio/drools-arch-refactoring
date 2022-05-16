package org.kie.dar.common.api.utils;/*
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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.GeneratedFinalResource;
import org.kie.dar.common.api.model.GeneratedIntermediateResource;
import org.kie.dar.common.api.model.GeneratedResource;
import org.kie.dar.common.api.model.GeneratedResources;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class JSONUtilsTest {

    @Test
    void getGeneratedResourceString() throws JsonProcessingException {
        String fullPath = "full/path";
        String type = "type";
        GeneratedResource generatedResource = new GeneratedIntermediateResource(fullPath, type);
        String expected = String.format("{\"step-type\":\"intermediate\",\"fullPath\":\"%s\",\"type\":\"%s\"}", fullPath, type);
        String retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);
        String frn = "this/is/frn";
        generatedResource = new GeneratedFinalResource(fullPath, type, frn);
        expected = String.format("{\"step-type\":\"final\",\"fullPath\":\"%s\",\"type\":\"%s\",\"frn\":\"%s\"}", fullPath, type, frn);
        retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);
    }

    @Test
    void getGeneratedResourceObject() throws JsonProcessingException {
        String generatedResourceString = "{\"step-type\":\"intermediate\",\"fullPath\":\"full/path\",\"type\":\"type\"}";
        GeneratedResource retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedIntermediateResource);
        generatedResourceString = "{\"step-type\":\"final\",\"fullPath\":\"full/path\",\"type\":\"type\",\"frn\":\"this/is/frn\"}}";
        retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedFinalResource);
    }

    @Test
    void getGeneratedResourcesString() throws JsonProcessingException {
        String fullPathIntermediate = "full/path/intermediate";
        String type = "type";
        GeneratedResource generatedIntermediateResource = new GeneratedIntermediateResource(fullPathIntermediate, type);
        String fullPathFinal = "full/path/final";
        String frn = "this/is/frn";
        GeneratedResource generatedFinalResource = new GeneratedFinalResource(fullPathFinal, type, frn);
        GeneratedResources generatedResources = new GeneratedResources();
        generatedResources.add(generatedIntermediateResource);
        generatedResources.add(generatedFinalResource);
        String retrieved = JSONUtils.getGeneratedResourcesString(generatedResources);
        System.out.println(retrieved);
        String expected1 = String.format("{\"step-type\":\"intermediate\",\"fullPath\":\"%s\",\"type\":\"%s\"}", fullPathIntermediate, type);
        String expected2 = String.format("{\"step-type\":\"final\",\"fullPath\":\"%s\",\"type\":\"%s\",\"frn\":\"%s\"}", fullPathFinal, type, frn);
        assertTrue(retrieved.contains(expected1));
        assertTrue(retrieved.contains(expected2));
    }

    @Test
    void getGeneratedResourcesStringFoo() throws JsonProcessingException {
        String fullPathIntermediate = "full/path/intermediate";
        String type = "type";
        GeneratedResource generatedIntermediateResource = new GeneratedIntermediateResource(fullPathIntermediate, type);
        String fullPathFinal = "full/path/final";
        String frn = "this/is/frn";
        GeneratedResource generatedFinalResource = new GeneratedFinalResource(fullPathFinal, type, frn);
        GeneratedResources generatedResources = new GeneratedResources();
        generatedResources.add(generatedIntermediateResource);
        generatedResources.add(generatedFinalResource);
        String retrieved = JSONUtils.getGeneratedResourcesString(generatedResources);
        System.out.println(retrieved);
        String expected1 = String.format("{\"step-type\":\"intermediate\",\"fullPath\":\"%s\",\"type\":\"%s\"}", fullPathIntermediate, type);
        String expected2 = String.format("{\"step-type\":\"final\",\"fullPath\":\"%s\",\"type\":\"%s\",\"frn\":\"%s\"}", fullPathFinal, type, frn);
        assertTrue(retrieved.contains(expected1));
        assertTrue(retrieved.contains(expected2));
    }

    @Test
    void getGeneratedResourcesObjectFromString() throws JsonProcessingException {
        String generatedResourcesString = "[{\"step-type\":\"final\",\"fullPath\":\"full/path/final\",\"type\":\"type\",\"frn\":\"this/is/frn\"},{\"step-type\":\"intermediate\",\"fullPath\":\"full/path/intermediate\",\"type\":\"type\"}]";
        GeneratedResources retrieved = JSONUtils.getGeneratedResourcesObject(generatedResourcesString);
        assertNotNull(retrieved);
        String fullPathIntermediate = "full/path/intermediate";
        String type = "type";
        GeneratedResource expected1 = new GeneratedIntermediateResource(fullPathIntermediate, type);
        String fullPathFinal = "full/path/final";
        String frn = "this/is/frn";
        GeneratedResource expected2 = new GeneratedFinalResource(fullPathFinal, type, frn);
        assertTrue(retrieved.contains(expected1));
        assertTrue(retrieved.contains(expected2));
    }

    @Test
    void getGeneratedResourcesObjectFromFile() throws JsonProcessingException {
        String fileName = "IndexFile.test_json";
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            assert resource != null;
            IndexFile indexFile = new IndexFile(resource.getFile());
            GeneratedResources retrieved = JSONUtils.getGeneratedResourcesObject(indexFile);
            assertNotNull(retrieved);
            String fullPathIntermediate = "full/path/intermediate";
            String type = "type";
            GeneratedResource expected1 = new GeneratedIntermediateResource(fullPathIntermediate, type);
            String fullPathFinal = "full/path/final";
            String frn = "this/is/frn";
            GeneratedResource expected2 = new GeneratedFinalResource(fullPathFinal, type, frn);
            assertTrue(retrieved.contains(expected1));
            assertTrue(retrieved.contains(expected2));
        } catch (Exception e) {
            fail(e);
        }
    }
}