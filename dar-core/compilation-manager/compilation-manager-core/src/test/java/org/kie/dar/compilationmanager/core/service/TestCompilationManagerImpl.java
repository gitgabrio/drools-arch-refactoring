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
import org.junit.jupiter.api.Test;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARProcessed;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.mocks.MockDARResourceA;
import org.kie.dar.compilationmanager.core.mocks.MockDARResourceB;
import org.kie.dar.compilationmanager.core.mocks.MockDARResourceC;
import org.kie.dar.compilationmanager.core.mocks.MockDARResourceD;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestCompilationManagerImpl {

    private static CompilationManager compilationManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private static final List<Class<? extends DARResource>> MANAGED_DAR_RESOURCES = Arrays.asList(MockDARResourceA.class, MockDARResourceB.class, MockDARResourceC.class);


    @BeforeAll
    static void setUp() {
        compilationManager = new CompilationManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(CompilationManager.class.getClassLoader());
    }

    @Test
    void processResource() {
        MANAGED_DAR_RESOURCES.forEach(managedResource -> {
            try {
                DARResource toProcess = managedResource.getDeclaredConstructor().newInstance();
                Optional<DARCompilationOutput> retrieved = compilationManager.processResource(toProcess, memoryCompilerClassLoader);
                assertTrue(retrieved.isPresent());
            } catch (Exception e) {
                fail(e);
            }
        });
        Optional<DARCompilationOutput> retrieved = compilationManager.processResource(new MockDARResourceD(), memoryCompilerClassLoader);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void processResources() {
        List<DARResource> toProcess = new ArrayList<>();
        MANAGED_DAR_RESOURCES.forEach(managedResource -> {
            try {
                DARResource toAdd = managedResource.getDeclaredConstructor().newInstance();
                toProcess.add(toAdd);
            } catch (Exception e) {
                fail(e);
            }
        });
        toProcess.add(new MockDARResourceD());
        List<DARCompilationOutput> retrieved = compilationManager.processResources(toProcess, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(MANAGED_DAR_RESOURCES.size(), retrieved.size());
    }
}