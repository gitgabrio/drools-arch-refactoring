package org.kie.foo.engine.runtime.service;/*
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
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.junit.jupiter.api.Assertions.*;

class KieRuntimeServiceFooTest {

    private static KieRuntimeService kieRuntimeService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieRuntimeService = new KieRuntimeServiceFoo();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageResource() {
        assertTrue(kieRuntimeService.canManageInput(new FRI("dar", "foo"), memoryCompilerClassLoader));
        assertFalse(kieRuntimeService.canManageInput(new FRI("dar", "notfoo"), memoryCompilerClassLoader));
        assertFalse(kieRuntimeService.canManageInput(new FRI("notdar", "foo"), memoryCompilerClassLoader));
    }

    @Test
    void evaluateInputExistingFooResources() {
        DARInputFoo toEvaluate = new DARInputFoo(new FRI("dar", "foo"), "InputData");
        DAROutputFoo retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(toEvaluate.getFRI(), retrieved.getFRI());
        assertEquals(toEvaluate.getInputData(), retrieved.getOutputData());
    }

    @Test
    void evaluateInputNotExistingFooResources() {
        try {
            DARInputFoo toEvaluate = new DARInputFoo(new FRI("DarFoo", "notfoo"), "InputData");
            kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieRuntimeServiceException);
        }
    }

}