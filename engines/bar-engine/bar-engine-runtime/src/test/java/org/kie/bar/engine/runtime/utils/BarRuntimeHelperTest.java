package org.kie.bar.engine.runtime.utils;/*
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
import org.kie.bar.engine.api.model.BarResources;
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.bar.engine.runtime.model.DAROutputBar;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.junit.jupiter.api.Assertions.*;

class BarRuntimeHelperTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void loadExistingBarResources() {
        BarResources retrieved = BarRuntimeHelper.loadBarResources(new FRI("dar", "bar"), memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getManagedResources().size());
        assertTrue(retrieved.getManagedResources().contains("BarResOne"));
        assertTrue(retrieved.getManagedResources().contains("BarResTwo"));
    }

    @Test
    void loadNotExistingBarResources() {
        try {
            BarRuntimeHelper.loadBarResources(new FRI("dar", "notbar"), memoryCompilerClassLoader);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieRuntimeServiceException);
        }
    }

    @Test
    void getDAROutput() {
        FRI fri = new FRI("dar", "bar");
        BarResources fooResources = BarRuntimeHelper.loadBarResources(fri, memoryCompilerClassLoader);
        DARInputBar darInputBar = new DARInputBar(fri, "InputData");
        DAROutputBar retrieved = BarRuntimeHelper.getDAROutput(fooResources, darInputBar);
        assertNotNull(retrieved);
        assertEquals(darInputBar.getFRI(), retrieved.getFRI());
        assertEquals(darInputBar.getInputData(), retrieved.getOutputData());
    }
}