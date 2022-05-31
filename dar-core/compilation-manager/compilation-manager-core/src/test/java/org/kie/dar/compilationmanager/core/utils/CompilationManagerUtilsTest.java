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
import org.kie.dar.common.api.model.*;
import org.kie.dar.compilationmanager.api.model.DARFinalOutputClassesContainer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;
import static org.kie.dar.common.api.utils.JSONUtils.writeGeneratedResourcesObject;

class CompilationManagerUtilsTest {

    private final static String modelType = "test";
    private final static FRI fri = new FRI("this/is/fri", modelType);
    private final static FRI notExistingfri = new FRI("this/is/fri", "notexisting");
    private final static Map<String, byte[]> compiledClassMap = IntStream.range(0, 3).boxed().collect(Collectors.toMap(integer -> "class_" + integer, integer -> new byte[0]));
    private final static DARFinalOutputClassesContainer finalOutput = getDARFinalOutputClassesContainer(fri);

//    @BeforeEach
//    public void init() {
//        try {
//            CompilationManagerUtils.getIndexFile(finalOutput).delete();
//        } catch (KieDARCommonException e) {
//            // Ignore
//        }
//    }

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
        DARFinalOutputClassesContainer notExistingOutput = getDARFinalOutputClassesContainer(notExistingfri);
        IndexFile retrieved = CompilationManagerUtils.getIndexFile(notExistingOutput);
        assertNotNull(retrieved);
        String expectedName = "IndexFile.notexisting_json";
        assertEquals(expectedName, retrieved.getName());
    }

    @Test
    void populateIndexFile() throws IOException {
        IndexFile toPopulate = CompilationManagerUtils.getIndexFile(finalOutput);
        GeneratedResources originalGeneratedResources = getGeneratedResourcesObject(toPopulate);
        int expectedResources = 2; // 1 final resource + 1 intermediate resources
        assertEquals(expectedResources, originalGeneratedResources.size());
        CompilationManagerUtils.populateIndexFile(toPopulate, finalOutput);
        GeneratedResources generatedResources = getGeneratedResourcesObject(toPopulate);
        expectedResources = 6; // 2 final resource + 4 class resources
        assertEquals(expectedResources, generatedResources.size());
        List<GeneratedExecutableResource> executableResources = generatedResources.stream().filter(generatedResource -> generatedResource instanceof GeneratedExecutableResource).map(GeneratedExecutableResource.class::cast).collect(Collectors.toList());
        expectedResources = 2; // 2 final resource
        assertEquals(expectedResources, executableResources.size());

        GeneratedExecutableResource finalResource = executableResources.stream().filter(generatedExecutableResource -> finalOutput.getFri().equals(generatedExecutableResource.getFri())).findFirst().orElse(null);

        commonEvaluateGeneratedExecutableResource(finalResource);
        List<GeneratedClassResource> classResources = generatedResources.stream().filter(generatedResource -> generatedResource instanceof GeneratedClassResource).map(GeneratedClassResource.class::cast).collect(Collectors.toList());
        expectedResources = 4; // 4 class resources
        assertEquals(expectedResources, classResources.size());

        List<GeneratedResource> classResourcesGenerated = classResources.stream().filter(generatedResource -> !generatedResource.getFullClassName().equals("type")).collect(Collectors.toList());
        commonEvaluateGeneratedIntermediateResources(classResourcesGenerated);

        // restore clean situation
        writeGeneratedResourcesObject(originalGeneratedResources, toPopulate);
    }

    @Test
    void populateGeneratedResources() {
        GeneratedResources toPopulate = new GeneratedResources();
        assertTrue(toPopulate.isEmpty());
        CompilationManagerUtils.populateGeneratedResources(toPopulate, finalOutput);
        int expectedResources = 4; // 1 final resource + 3 intermediate resources
        assertEquals(expectedResources, toPopulate.size());
        GeneratedResource finalResource = toPopulate.stream().filter(generatedResource -> generatedResource instanceof GeneratedExecutableResource).findFirst().orElse(null);
        commonEvaluateGeneratedExecutableResource(finalResource);
        List<GeneratedResource> classResources = toPopulate.stream().filter(generatedResource -> generatedResource instanceof GeneratedClassResource).map(GeneratedClassResource.class::cast).collect(Collectors.toList());
        commonEvaluateGeneratedIntermediateResources(classResources);
    }

    @Test
    void getGeneratedResource() {
        GeneratedResource retrieved = CompilationManagerUtils.getGeneratedResource(finalOutput);
        commonEvaluateGeneratedExecutableResource(retrieved);
    }

    @Test
    void getGeneratedResources() {
        List<GeneratedResource> retrieved = CompilationManagerUtils.getGeneratedResources(finalOutput);
        commonEvaluateGeneratedIntermediateResources(retrieved);
    }

    @Test
    void getGeneratedIntermediateResource() {
        String className = "className";
        GeneratedClassResource retrieved = CompilationManagerUtils.getGeneratedClassResource(className);
        assertNotNull(retrieved);
        assertEquals(className, retrieved.getFullClassName());
    }

    private void commonEvaluateGeneratedExecutableResource(GeneratedResource generatedResource) {
        assertNotNull(generatedResource);
        assertTrue(generatedResource instanceof GeneratedExecutableResource);
        assertEquals(finalOutput.getFri(), ((GeneratedExecutableResource) generatedResource).getFri());
    }

    private void commonEvaluateGeneratedIntermediateResources(List<GeneratedResource> retrieved) {
        assertNotNull(retrieved);
        assertEquals(compiledClassMap.size(), retrieved.size());
        compiledClassMap.keySet().forEach(fullClassName -> {
            assertTrue(retrieved.stream().filter(GeneratedClassResource.class::isInstance).map(GeneratedClassResource.class::cast).anyMatch(generatedResource -> generatedResource.getFullClassName().equals(fullClassName)));
        });
    }

    private static DARFinalOutputClassesContainer getDARFinalOutputClassesContainer(FRI usedFri) {
        return new DARFinalOutputClassesContainer(usedFri, usedFri.getModel() +"Resources", compiledClassMap) {
        };
    }
}