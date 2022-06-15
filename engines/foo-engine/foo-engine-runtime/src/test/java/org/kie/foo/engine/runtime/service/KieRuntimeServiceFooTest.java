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
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KieRuntimeServiceFooTest {

    private static KieRuntimeServiceFoo kieRuntimeService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieRuntimeService = new KieRuntimeServiceFoo();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageResource() {
        DARInputFoo toEvaluate = new DARInputFoo(new FRI("dar", "foo"), "InputData");
        assertThat(kieRuntimeService.canManageInput(toEvaluate, memoryCompilerClassLoader)).isTrue();
        toEvaluate = new DARInputFoo(new FRI("dar", "notfoo"), "InputData");
        assertThat(kieRuntimeService.canManageInput(toEvaluate, memoryCompilerClassLoader)).isFalse();
    }

    @Test
    void evaluateInputExistingFooResources() {
        DARInputFoo toEvaluate = new DARInputFoo(new FRI("dar", "foo"), "InputData");
        Optional<DAROutputFoo> retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrieved).isPresent();
        DAROutputFoo darOutputFoo = retrieved.get();
        assertThat(darOutputFoo.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(darOutputFoo.getOutputData()).isEqualTo(toEvaluate.getInputData());
    }

    @Test
    void evaluateInputNotExistingFooResources() {
        DARInputFoo toEvaluate = new DARInputFoo(new FRI("DarFoo", "notfoo"), "InputData");
        Optional<DAROutputFoo> retrieved = kieRuntimeService.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isNotPresent();
    }

}