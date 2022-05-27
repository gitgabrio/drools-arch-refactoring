/*
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
package org.kie.dar.common.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.*;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class JSONUtilsTest {

    @Test
    void getGeneratedResourceString() throws JsonProcessingException {
        String fullClassName = "full.class.Name";
        GeneratedResource generatedResource = new GeneratedClassResource(fullClassName);
        String expected = String.format("{\"step-type\":\"class\",\"fullClassName\":\"%s\"}", fullClassName);
        String retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);

        String fri = "this/is/fri";
        String target = "foo";
        generatedResource = new GeneratedRedirectResource(fri, target);
        expected = String.format("{\"step-type\":\"redirect\",\"fri\":\"%s\",\"target\":\"%s\"}", fri, target);
        retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);

        String model = "foo";
        generatedResource = new GeneratedExecutableResource(fri, model, fullClassName);
        expected = String.format("{\"step-type\":\"executable\",\"fri\":\"%s\",\"model\":\"%s\",\"fullClassName\":\"%s\"}", fri, model, fullClassName);
        retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);
    }

    @Test
    void getGeneratedResourceObject() throws JsonProcessingException {
        String generatedResourceString = "{\"step-type\":\"redirect\",\"fri\":\"this/is/fri\",\"target\":\"foo\"}";
        GeneratedResource retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedRedirectResource);

        generatedResourceString = "{\"step-type\":\"class\",\"fullClassName\":\"full.class.Name\"}\"";
        retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedClassResource);

        generatedResourceString = "{\"step-type\":\"executable\",\"fri\":\"this/is/fri\",\"model\":\"foo\",\"fullClassName\":\"full.class.Name\"}\"}}";
        retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedExecutableResource);
    }

    @Test
    void getGeneratedResourcesString() throws JsonProcessingException {
        String fullClassName = "full.class.Name";
        GeneratedResource generatedIntermediateResource = new GeneratedClassResource(fullClassName);
        String fri = "this/is/fri";
        String model = "foo";
        GeneratedResource generatedFinalResource = new GeneratedExecutableResource(fri, model, fullClassName);
        GeneratedResources generatedResources = new GeneratedResources();
        generatedResources.add(generatedIntermediateResource);
        generatedResources.add(generatedFinalResource);
        String retrieved = JSONUtils.getGeneratedResourcesString(generatedResources);
        String expected1 = String.format("{\"step-type\":\"class\",\"fullClassName\":\"%s\"}", fullClassName);
        String expected2 = String.format("{\"step-type\":\"executable\",\"fri\":\"%s\",\"model\":\"%s\",\"fullClassName\":\"%s\"}", fri, model, fullClassName);
        assertTrue(retrieved.contains(expected1));
        assertTrue(retrieved.contains(expected2));
    }

    @Test
    void getGeneratedResourcesObjectFromString() throws JsonProcessingException {
        String generatedResourcesString = "[{\"step-type\":\"executable\",\"fri\":\"this/is/fri\",\"model\":\"foo\"},{\"step-type\":\"class\",\"fullClassName\":\"full.class.Name\"}]";
        GeneratedResources retrieved = JSONUtils.getGeneratedResourcesObject(generatedResourcesString);
        assertNotNull(retrieved);
        String fullClassName = "full.class.Name";
        GeneratedResource expected1 = new GeneratedClassResource(fullClassName);
        String fri = "this/is/fri";
        String model = "foo";
        GeneratedResource expected2 = new GeneratedExecutableResource(fri, model, fullClassName);
        assertTrue(retrieved.contains(expected1));
        assertTrue(retrieved.contains(expected2));
    }

    @Test
    void getGeneratedResourcesObjectFromFile() {
        String fileName = "IndexFile.test_json";
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            assert resource != null;
            IndexFile indexFile = new IndexFile(new File(resource.toURI()));
            GeneratedResources retrieved = JSONUtils.getGeneratedResourcesObject(indexFile);
            assertNotNull(retrieved);
            String fullClassName = "full.class.Name";
            GeneratedResource expected1 = new GeneratedClassResource(fullClassName);
            String fri = "this/is/fri";
            String model = "foo";
            GeneratedResource expected2 = new GeneratedExecutableResource(fri, model, fullClassName);
            assertTrue(retrieved.contains(expected1));
            assertTrue(retrieved.contains(expected2));
        } catch (Exception e) {
            fail(e);
        }
    }
}