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

        String model = "foo";
        FRI fri = new FRI("this/is/fri", model);
        String target = "foo";
        generatedResource = new GeneratedRedirectResource(fri, target);
        expected = String.format("{\"step-type\":\"redirect\",\"fri\":%s,\"target\":\"%s\"}", JSONUtils.getFRIString(fri), target);
        retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);

        generatedResource = new GeneratedExecutableResource(fri, fullClassName);
        expected = String.format("{\"step-type\":\"executable\",\"fri\":%s,\"fullClassName\":\"%s\"}", JSONUtils.getFRIString(fri), fullClassName);
        retrieved = JSONUtils.getGeneratedResourceString(generatedResource);
        assertEquals(expected, retrieved);
    }

    @Test
    void getGeneratedResourceObject() throws JsonProcessingException {
        String generatedResourceString = "{\"step-type\":\"redirect\",\"fri\":{\"basePath\":\"this/is/fri\",\"fri\":\"this/is/fri_foo\"},\"target\":\"foo\"}";
        GeneratedResource retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedRedirectResource);

        generatedResourceString = "{\"step-type\":\"class\",\"fullClassName\":\"full.class.Name\"}\"";
        retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedClassResource);

        generatedResourceString = "{\"step-type\":\"executable\",\"fri\":{\"basePath\":\"this/is/fri\",\"fri\":\"this/is/fri_foo\",\"model\":\"foo\"},\"fullClassName\":\"full.class.Name\"}";
        retrieved = JSONUtils.getGeneratedResourceObject(generatedResourceString);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof GeneratedExecutableResource);
    }

    @Test
    void getGeneratedResourcesString() throws JsonProcessingException {
        String fullClassName = "full.class.Name";
        GeneratedResource generatedIntermediateResource = new GeneratedClassResource(fullClassName);
        String model = "foo";
        FRI fri = new FRI("this/is/fri", model);
        GeneratedResource generatedFinalResource = new GeneratedExecutableResource(fri, fullClassName);
        GeneratedResources generatedResources = new GeneratedResources();
        generatedResources.add(generatedIntermediateResource);
        generatedResources.add(generatedFinalResource);
        String retrieved = JSONUtils.getGeneratedResourcesString(generatedResources);
        String expected1 = String.format("{\"step-type\":\"class\",\"fullClassName\":\"%s\"}", fullClassName);
        String expected2 = String.format("{\"step-type\":\"executable\",\"fri\":%s,\"fullClassName\":\"%s\"}", JSONUtils.getFRIString(fri), fullClassName);
        assertTrue(retrieved.contains(expected1));
        assertTrue(retrieved.contains(expected2));
    }

    @Test
    void getGeneratedResourcesObjectFromString() throws JsonProcessingException {
        String generatedResourcesString = "[{\"step-type\":\"executable\",\"fri\":{\"basePath\":\"this/is/fri\",\"fri\":\"/foo/this/is/fri\"}},{\"step-type\":\"class\",\"fullClassName\":\"full.class.Name\"}]";
        GeneratedResources retrieved = JSONUtils.getGeneratedResourcesObject(generatedResourcesString);
        assertNotNull(retrieved);
        String fullClassName = "full.class.Name";
        GeneratedResource expected1 = new GeneratedClassResource(fullClassName);
        String model = "foo";
        FRI fri = new FRI("this/is/fri", model);
        GeneratedResource expected2 = new GeneratedExecutableResource(fri, fullClassName);
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
            String model = "foo";
            FRI fri = new FRI("this/is/fri", model);
            GeneratedResource expected2 = new GeneratedExecutableResource(fri, fullClassName);
            assertTrue(retrieved.contains(expected1));
            assertTrue(retrieved.contains(expected2));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void getFRIString() throws JsonProcessingException {
        String model = "foo";
        String basePath = "this/is/fri";
        FRI fri = new FRI(basePath, model);
        String retrieved = JSONUtils.getFRIString(fri);
        String expected = String.format("{\"basePath\":\"%1$s\",\"model\":\"%2$s\",\"fri\":\"/%2$s%1$s\"}", "/" + basePath, model);
        assertEquals(expected, retrieved);
    }

    @Test
    void getFRIObject() throws JsonProcessingException {
        String friString = "{\"basePath\":\"this/is/fri\",\"model\":\"foo\",\"fri\":\"/foo/this/is/fri\"}";
        FRI retrieved = JSONUtils.getFRIObject(friString);
        assertNotNull(retrieved);
        String expected = "foo";
        assertEquals(expected, retrieved.getModel());
        expected = "this/is/fri";
        assertEquals(expected, retrieved.getBasePath());
        expected = "/foo/this/is/fri";
        assertEquals(expected, retrieved.getFri());
    }
}