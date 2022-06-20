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
import org.kie.bar.engine.runtime.model.EfestoInputBar;
import org.kie.bar.engine.runtime.model.EfestoOutputBar;
import org.kie.efesto.common.api.model.FRI;
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
        EfestoInputBar toEvaluate = new EfestoInputBar(new FRI("/bar/efesto", "bar"), "InputData");
        assertThat(kieRuntimeService.canManageInput(toEvaluate, memoryCompilerClassLoader)).isTrue();
        toEvaluate = new EfestoInputBar(new FRI("/bar/efesto", "notbar"), "InputData");
        assertThat(kieRuntimeService.canManageInput(toEvaluate, memoryCompilerClassLoader)).isFalse();
        toEvaluate = new EfestoInputBar(new FRI("darfoo", "bar"), "InputData");
        assertThat(kieRuntimeService.canManageInput(toEvaluate, memoryCompilerClassLoader)).isFalse();
    }

    @Test
    void evaluateInputExistingBarResources() {
        EfestoInputBar toEvaluate = new EfestoInputBar(new FRI("/efesto", "bar"), "InputData");
        Optional<EfestoOutputBar> retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrieved).isPresent();
        EfestoOutputBar darOutputBar = retrieved.get();
        assertThat(darOutputBar.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(darOutputBar.getOutputData()).isEqualTo(toEvaluate.getInputData());

    }

    @Test
    void evaluateInputNotExistingBarResources() {
        EfestoInputBar toEvaluate = new EfestoInputBar(new FRI("/bar/efesto", "notbar"), "InputData");
        Optional<EfestoOutputBar> retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isNotPresent();
    }

}