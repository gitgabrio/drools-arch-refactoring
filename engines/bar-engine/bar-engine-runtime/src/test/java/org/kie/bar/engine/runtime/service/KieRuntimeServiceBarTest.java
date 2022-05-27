package org.kie.bar.engine.runtime.service;/*
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
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.junit.jupiter.api.Assertions.*;

class KieRuntimeServiceBarTest {

    private static KieRuntimeService kieRuntimeService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieRuntimeService = new KieRuntimeServiceBar();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageResource() {
        assertTrue(kieRuntimeService.canManageInput("/bar/dar", memoryCompilerClassLoader));
        assertFalse(kieRuntimeService.canManageInput("/notbar/dar", memoryCompilerClassLoader));
        assertFalse(kieRuntimeService.canManageInput("/bar/darfoo", memoryCompilerClassLoader));
    }

    @Test
    void evaluateInputExistingBarResources() {
        DARInput toEvaluate = new DARInputBar("/bar/dar", "InputData");
        DAROutput retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(toEvaluate.getFullResourceIdentifier(), retrieved.getFullResourceName());
        assertEquals(toEvaluate.getInputData(), retrieved.getOutputData());

    }

    @Test
    void evaluateInputNotExistingBarResources() {
        try {
            DARInput toEvaluate = new DARInputBar("DarNotBar", "InputData");
            kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieRuntimeServiceException);
        }
    }

}