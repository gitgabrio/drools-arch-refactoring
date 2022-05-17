package org.kie.dar.compilationmanager.core.utils;/*
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
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.GeneratedFinalResource;
import org.kie.dar.common.api.model.GeneratedIntermediateResource;
import org.kie.dar.common.api.model.GeneratedResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.compilationmanager.api.model.DARFinalOutputClassesContainer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;

class CompilationManagerUtilsTest {

    private final static String fri = "fri";
    private final static String modelType = "test";
    private final static Map<String, byte[]> compiledClassMap = IntStream.range(0, 3)
            .boxed()
            .collect(Collectors.toMap(integer -> "class_" + integer,
                    integer -> new byte[0]));
    private final static DARFinalOutputClassesContainer finalOutput = getDARFinalOutputClassesContainer(modelType);

    @Test
    void populateIndexFilesWithProcessedResource() {
    }

    @Test
    void getIndexFileFromFinalOutput() {
    }

    @Test
    void getIndexFileExisting() {
        IndexFile retrieved = CompilationManagerUtils.getIndexFile(finalOutput);
        assertNotNull(retrieved);
        String expectedName = "IndexFile.test_json";
        assertEquals(expectedName, retrieved.getName());
    }

    @Test
    void getIndexFileNotExisting() {
        String notExistingModelType = "notexisting";
        DARFinalOutputClassesContainer notExistingOutput = getDARFinalOutputClassesContainer(notExistingModelType);
        IndexFile retrieved = CompilationManagerUtils.getIndexFile(notExistingOutput);
        assertNotNull(retrieved);
        String expectedName = "IndexFile.notexisting_json";
        assertEquals(expectedName, retrieved.getName());
    }

    @Test
    void populateIndexFile() throws IOException {
        IndexFile toPopulate = CompilationManagerUtils.getIndexFile(finalOutput);
        GeneratedResources generatedResources = getGeneratedResourcesObject(toPopulate);
        int expectedResources = 2; // 1 final resource + 1 intermediate resources
        assertEquals(expectedResources, generatedResources.size());
        CompilationManagerUtils.populateIndexFile(toPopulate, finalOutput);
        generatedResources = getGeneratedResourcesObject(toPopulate);
        expectedResources = 6; // 2 final resource + 4 intermediate resources
        assertEquals(expectedResources, generatedResources.size());
//        GeneratedResource finalResource = toPopulate.stream()
//                .filter(generatedResource -> generatedResource instanceof GeneratedFinalResource)
//                .findFirst().orElse(null);
//        commonEvaluateGeneratedFinalResource(finalResource, finalOutput);
//        List<GeneratedResource> intermediateResources = toPopulate.stream()
//                .filter(generatedResource -> generatedResource instanceof GeneratedIntermediateResource)
//                .collect(Collectors.toList());
//        commonEvaluateGeneratedIntermediateResources(intermediateResources);
    }

    @Test
    void populateGeneratedResources() {
        GeneratedResources toPopulate = new GeneratedResources();
        assertTrue(toPopulate.isEmpty());
        CompilationManagerUtils.populateGeneratedResources(toPopulate, finalOutput);
        int expectedResources = 4; // 1 final resource + 3 intermediate resources
        assertEquals(expectedResources, toPopulate.size());
        GeneratedResource finalResource = toPopulate.stream()
                .filter(generatedResource -> generatedResource instanceof GeneratedFinalResource)
                .findFirst().orElse(null);
        commonEvaluateGeneratedFinalResource(finalResource, finalOutput);
        List<GeneratedResource> intermediateResources = toPopulate.stream()
                .filter(generatedResource -> generatedResource instanceof GeneratedIntermediateResource)
                .collect(Collectors.toList());
        commonEvaluateGeneratedIntermediateResources(intermediateResources);
    }

    @Test
    void getGeneratedResource() {
        GeneratedResource retrieved = CompilationManagerUtils.getGeneratedResource(finalOutput);
        commonEvaluateGeneratedFinalResource(retrieved, finalOutput);
    }

    @Test
    void getGeneratedResources() {
        List<GeneratedResource> retrieved = CompilationManagerUtils.getGeneratedResources(finalOutput);
        commonEvaluateGeneratedIntermediateResources(retrieved);
    }

    @Test
    void getGeneratedIntermediateResource() {
        String className = "className";
        GeneratedResource retrieved = CompilationManagerUtils.GeneratedIntermediateResource(className);
        commonEvaluateGeneratedIntermediateResource(retrieved, className);
    }

    private void commonEvaluateGeneratedFinalResource(GeneratedResource generatedResource, DARFinalOutputClassesContainer finalOutput) {
        assertNotNull(generatedResource);
        assertTrue(generatedResource instanceof GeneratedFinalResource);
        assertEquals(finalOutput.toString(), generatedResource.getFullPath());
        assertEquals(finalOutput.getModelType(), generatedResource.getType());
        assertEquals(finalOutput.getFri(), ((GeneratedFinalResource) generatedResource).getFri());
    }

    private void commonEvaluateGeneratedIntermediateResources(List<GeneratedResource> retrieved) {
        assertNotNull(retrieved);
        assertEquals(compiledClassMap.size(), retrieved.size());
        compiledClassMap.keySet().forEach(fullPath -> {
            GeneratedResource mappedResource = retrieved.stream().filter(generatedResource -> generatedResource.getFullPath().equals(fullPath)).findFirst().orElse(null);
            commonEvaluateGeneratedIntermediateResource(mappedResource, fullPath);
        });
    }

    private void commonEvaluateGeneratedIntermediateResource(GeneratedResource generatedResource, String expectedFullPath) {
        assertNotNull(generatedResource);
        assertTrue(generatedResource instanceof GeneratedIntermediateResource);
        assertEquals(expectedFullPath, generatedResource.getFullPath());
        assertEquals("class", generatedResource.getType());
    }

    private static DARFinalOutputClassesContainer getDARFinalOutputClassesContainer(String outputModelType) {
        return new DARFinalOutputClassesContainer(fri, outputModelType, compiledClassMap) {
        };
    }
}