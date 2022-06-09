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
package org.kie.bar.engine.runtime.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.bar.engine.runtime.model.DAROutputBar;
import org.kie.dar.common.api.model.FRI;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KieRuntimeServiceBarTest {

    private static KieRuntimeServiceBar kieRuntimeService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieRuntimeService = new KieRuntimeServiceBar();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageResource() {
        assertThat(kieRuntimeService.canManageInput(new FRI("/bar/dar", "bar"), memoryCompilerClassLoader)).isTrue();
        assertThat(kieRuntimeService.canManageInput(new FRI("/bar/dar", "notbar"), memoryCompilerClassLoader)).isFalse();
        assertThat(kieRuntimeService.canManageInput(new FRI("darfoo", "bar"), memoryCompilerClassLoader)).isFalse();
    }

    @Test
    void evaluateInputExistingBarResources() {
        DARInputBar toEvaluate = new DARInputBar(new FRI("/dar", "bar"), "InputData");
        Optional<DAROutputBar> retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrieved).isPresent();
        DAROutputBar darOutputBar = retrieved.get();
        assertThat(darOutputBar.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(darOutputBar.getOutputData()).isEqualTo(toEvaluate.getInputData());

    }

    @Test
    void evaluateInputNotExistingBarResources() {
        DARInputBar toEvaluate = new DARInputBar(new FRI("/bar/dar", "notbar"), "InputData");
        Optional<DAROutputBar> retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isNotPresent();
    }

}