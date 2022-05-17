package org.kie.dar.compilationmanager.core.service;/*
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARIntermediateOutput;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.mocks.MockDARIntermediateOutputC;
import org.kie.dar.compilationmanager.core.mocks.MockDARIntermediateOutputA;
import org.kie.dar.compilationmanager.core.mocks.MockDARIntermediateOutputB;
import org.kie.dar.compilationmanager.core.mocks.MockDARIntermediateOutputD;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;

class TestCompilationManagerImpl {

    private static CompilationManager compilationManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private static final List<Class<? extends DARIntermediateOutput>> MANAGED_DAR_RESOURCES = Arrays.asList(MockDARIntermediateOutputA.class, MockDARIntermediateOutputB.class, MockDARIntermediateOutputC.class);


    @BeforeAll
    static void setUp() {
        compilationManager = new CompilationManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(CompilationManager.class.getClassLoader());
    }

    @Test
    void processResource() {
        MANAGED_DAR_RESOURCES.forEach(managedResource -> {
            try {
                DARIntermediateOutput toProcess = managedResource.getDeclaredConstructor().newInstance();
                List<IndexFile> retrieved = compilationManager.processResource(toProcess, memoryCompilerClassLoader);
                assertEquals(1, retrieved.size());
                retrieved.get(0).delete();
            } catch (Exception e) {
                fail(e);
            }
        });
        List<IndexFile> retrieved = compilationManager.processResource(new MockDARIntermediateOutputD(), memoryCompilerClassLoader);
        assertTrue(retrieved.isEmpty());
    }

    // TODO restore
//    @Test
//    void processResources() {
//        List<DARIntermediateOutput> toProcess = new ArrayList<>();
//        MANAGED_DAR_RESOURCES.forEach(managedResource -> {
//            try {
//                DARIntermediateOutput toAdd = managedResource.getDeclaredConstructor().newInstance();
//                toProcess.add(toAdd);
//            } catch (Exception e) {
//                fail(e);
//            }
//        });
//        toProcess.add(new MockDARIntermediateOutputD());
//        List<DARCompilationOutput> retrieved = compilationManager.processResources(toProcess, memoryCompilerClassLoader);
//        assertNotNull(retrieved);
//        assertEquals(MANAGED_DAR_RESOURCES.size(), retrieved.size());
//    }
}