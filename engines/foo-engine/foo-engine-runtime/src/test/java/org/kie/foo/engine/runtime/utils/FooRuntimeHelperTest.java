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
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.foo.engine.api.model.FooResources;
import org.kie.foo.engine.runtime.model.EfestoInputFoo;
import org.kie.foo.engine.runtime.model.EfestoOutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class FooRuntimeHelperTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void loadExistingFooResources() {
        FooResources retrieved = FooRuntimeHelper.loadFooResources(new FRI("efesto", "foo"), memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getManagedResources().size()).isEqualTo(2);
        assertThat(retrieved.getManagedResources().contains("FooResOne")).isTrue();
        assertThat(retrieved.getManagedResources().contains("FooResTwo")).isTrue();
    }

    @Test
    void loadNotExistingFooResources() {
        try {
            FooRuntimeHelper.loadFooResources(new FRI("efesto", "notfoo"), memoryCompilerClassLoader);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertThat(e instanceof KieRuntimeServiceException).isTrue();
        }
    }

    @Test
    void getEfestoOutput() {
        FRI fri = new FRI("efesto", "foo");
        FooResources fooResources = FooRuntimeHelper.loadFooResources(fri, memoryCompilerClassLoader);
        EfestoInputFoo darInputFoo = new EfestoInputFoo(fri, "InputData");
        EfestoOutputFoo retrieved = FooRuntimeHelper.getEfestoOutput(fooResources, darInputFoo);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getFRI()).isEqualTo(darInputFoo.getFRI());
        assertThat(retrieved.getOutputData()).isEqualTo(darInputFoo.getInputData());
    }
}