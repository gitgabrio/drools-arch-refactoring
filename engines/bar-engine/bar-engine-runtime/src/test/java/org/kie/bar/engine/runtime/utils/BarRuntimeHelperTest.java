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
import org.kie.bar.engine.runtime.model.EfestoInputBar;
import org.kie.bar.engine.runtime.model.EfestoOutputBar;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.AbstractEfestoInput;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class BarRuntimeHelperTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManage() {
        FRI fri = new FRI("/bar/efesto", "bar");
        AbstractEfestoInput darInputBar = new EfestoInputBar(fri, "InputData");
        assertThat(BarRuntimeHelper.canManage(darInputBar)).isTrue();
        darInputBar = new AbstractEfestoInput(fri, "InputData") {};
        assertThat(BarRuntimeHelper.canManage(darInputBar)).isFalse();
        fri = new FRI("/bar/efesto", "notbar");
        darInputBar = new EfestoInputBar(fri, "InputData");
        assertThat(BarRuntimeHelper.canManage(darInputBar)).isFalse();
    }

    @Test
    void loadExistingBarResources() {
        BarResources retrieved = BarRuntimeHelper.loadBarResources(new FRI("efesto", "bar"), memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getManagedResources()).hasSize(2);
        assertThat(retrieved.getManagedResources().contains("BarResOne")).isTrue();
        assertThat(retrieved.getManagedResources().contains("BarResTwo")).isTrue();
    }

    @Test
    void loadNotExistingBarResources() {
        try {
            BarRuntimeHelper.loadBarResources(new FRI("efesto", "notbar"), memoryCompilerClassLoader);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(KieRuntimeServiceException.class);
        }
    }

    @Test
    void getEfestoOutput() {
        FRI fri = new FRI("efesto", "bar");
        BarResources barResources = BarRuntimeHelper.loadBarResources(fri, memoryCompilerClassLoader);
        EfestoInputBar darInputBar = new EfestoInputBar(fri, "InputData");
        EfestoOutputBar retrieved = BarRuntimeHelper.getEfestoOutput(barResources, darInputBar);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getFRI()).isEqualTo(darInputBar.getFRI());
        assertThat(retrieved.getOutputData()).isEqualTo(darInputBar.getInputData());
    }
}