package org.kie.foo.engine.runtime.utils;/*
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
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.foo.engine.api.model.FooResources;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.junit.jupiter.api.Assertions.*;

class FooRuntimeHelperTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void loadExistingFooResources() {
        FooResources retrieved = FooRuntimeHelper.loadFooResources("DarFoo", memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getManagedResources().size());
        assertTrue(retrieved.getManagedResources().contains("FooResOne"));
        assertTrue(retrieved.getManagedResources().contains("FooResTwo"));
    }

    @Test
    void loadNotExistingFooResources() {
        try {
            FooRuntimeHelper.loadFooResources("DarNotFoo", memoryCompilerClassLoader);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieRuntimeServiceException);
        }
    }

    @Test
    void getDAROutput() {
        FooResources fooResources = FooRuntimeHelper.loadFooResources("DarFoo", memoryCompilerClassLoader);
        DARInputFoo darInputFoo = new DARInputFoo("DarFoo", "InputData");
        DAROutputFoo retrieved = FooRuntimeHelper.getDAROutput(fooResources, darInputFoo);
        assertNotNull(retrieved);
        assertEquals(darInputFoo.getFullResourceIdentifier(), retrieved.getFullResourceName());
        assertEquals(darInputFoo.getInputData(), retrieved.getOutputData());
    }
}