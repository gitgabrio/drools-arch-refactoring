package org.kie.dar.runtimemanager.core.service;/*
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
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.RuntimeManager;
import org.kie.dar.runtimemanager.api.mocks.MockDARInputA;
import org.kie.dar.runtimemanager.api.mocks.MockDARInputB;
import org.kie.dar.runtimemanager.api.mocks.MockDARInputC;
import org.kie.dar.runtimemanager.api.mocks.MockDARInputD;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRuntimeManagerImpl {

    private static RuntimeManager runtimeManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private static final List<Class<? extends DARInput>> MANAGED_DAR_INPUTS = Arrays.asList(MockDARInputA.class,
            MockDARInputB.class,
            MockDARInputC.class);


    @BeforeAll
    static void setUp() {
        runtimeManager = new RuntimeManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void evaluateInput() {
        MANAGED_DAR_INPUTS.forEach(managedInput -> {
            try {
                DARInput toProcess = managedInput.getDeclaredConstructor().newInstance();
                Optional<DAROutput> retrieved = runtimeManager.evaluateInput(toProcess, memoryCompilerClassLoader);
                assertTrue(retrieved.isPresent());
            } catch (Exception e) {
                fail(e);
            }
        });
        Optional<DAROutput> retrieved = runtimeManager.evaluateInput(new MockDARInputD(), memoryCompilerClassLoader);
        assertThat(retrieved.isEmpty()).isTrue();
    }

    @Test
    void evaluateInputs() {
        List<DARInput> toProcess = new ArrayList<>();
        MANAGED_DAR_INPUTS.forEach(managedInput -> {
            try {
                DARInput toAdd = managedInput.getDeclaredConstructor().newInstance();
                toProcess.add(toAdd);
            } catch (Exception e) {
                fail(e);
            }
        });
        toProcess.add(new MockDARInputD());
        List<DAROutput> retrieved = runtimeManager.evaluateInputs(toProcess, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.size()).isEqualTo(MANAGED_DAR_INPUTS.size());
    }
}