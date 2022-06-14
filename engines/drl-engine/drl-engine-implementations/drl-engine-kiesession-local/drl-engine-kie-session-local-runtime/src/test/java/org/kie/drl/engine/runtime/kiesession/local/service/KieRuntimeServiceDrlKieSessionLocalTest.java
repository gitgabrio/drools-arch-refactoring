package org.kie.drl.engine.runtime.kiesession.local.service;/*
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
import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.model.FRI;
import org.kie.drl.engine.runtime.kiesession.local.model.DARInputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.model.DAROutputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.utils.DrlRuntimeHelper;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.kie.drl.engine.runtime.kiesession.local.utils.DrlRuntimeHelper.SUBPATH;

class KieRuntimeServiceDrlKieSessionLocalTest {

    private static final String basePath = "TestingRule";
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;
    private static KieRuntimeServiceDrlKieSessionLocal kieRuntimeServiceDrlKieSessionLocal;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
        kieRuntimeServiceDrlKieSessionLocal = new KieRuntimeServiceDrlKieSessionLocal();
    }

    @Test
    void canManageInput() {
        FRI fri = new FRI(SUBPATH + FRI.SLASH + basePath, "drl");
        assertThat(kieRuntimeServiceDrlKieSessionLocal.canManageInput(fri, memoryCompilerClassLoader)).isTrue();
        fri = new FRI(basePath, "drl");
        assertThat(kieRuntimeServiceDrlKieSessionLocal.canManageInput(fri, memoryCompilerClassLoader)).isFalse();
        fri = new FRI(SUBPATH + FRI.SLASH + "notexisting", "drl");
        assertThat(kieRuntimeServiceDrlKieSessionLocal.canManageInput(fri, memoryCompilerClassLoader)).isFalse();
    }

    @Test
    void evaluateInput() {
        DARInputDrlKieSessionLocal darInputDrlKieSessionLocal = new DARInputDrlKieSessionLocal(new FRI(SUBPATH + FRI.SLASH + basePath, "drl"), "");
        Optional<DAROutputDrlKieSessionLocal> retrieved = kieRuntimeServiceDrlKieSessionLocal.evaluateInput(darInputDrlKieSessionLocal, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isPresent();
        assertThat(retrieved.get().getOutputData()).isNotNull().isInstanceOf(KieSession.class);
        darInputDrlKieSessionLocal = new DARInputDrlKieSessionLocal(new FRI(basePath, "drl"), "");
        retrieved = kieRuntimeServiceDrlKieSessionLocal.evaluateInput(darInputDrlKieSessionLocal, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isNotPresent();
        darInputDrlKieSessionLocal = new DARInputDrlKieSessionLocal(new FRI(SUBPATH + FRI.SLASH + "notexisting", "drl"), "");
        retrieved = kieRuntimeServiceDrlKieSessionLocal.evaluateInput(darInputDrlKieSessionLocal, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isNotPresent();
    }
}