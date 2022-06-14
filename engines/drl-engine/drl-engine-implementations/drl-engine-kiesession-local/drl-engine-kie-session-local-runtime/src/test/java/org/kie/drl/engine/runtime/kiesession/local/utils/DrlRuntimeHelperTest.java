package org.kie.drl.engine.runtime.kiesession.local.utils;/*
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
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.kie.drl.engine.runtime.kiesession.local.utils.DrlRuntimeHelper.SUBPATH;

class DrlRuntimeHelperTest {

    private static final String basePath = "TestingRule";
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManage() {
        FRI fri = new FRI(SUBPATH + FRI.SLASH + basePath, "drl");
        assertThat(DrlRuntimeHelper.canManage(fri)).isTrue();
        fri = new FRI(basePath, "drl");
        assertThat(DrlRuntimeHelper.canManage(fri)).isFalse();
        fri = new FRI(SUBPATH + FRI.SLASH + "notexisting", "drl");
        assertThat(DrlRuntimeHelper.canManage(fri)).isFalse();
    }

    @Test
    void execute() {
        DARInputDrlKieSessionLocal darInputDrlKieSessionLocal = new DARInputDrlKieSessionLocal(new FRI(SUBPATH + FRI.SLASH + basePath, "drl"), "");
        Optional<DAROutputDrlKieSessionLocal> retrieved = DrlRuntimeHelper.execute(darInputDrlKieSessionLocal, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isPresent();
        assertThat(retrieved.get().getOutputData()).isNotNull().isInstanceOf(KieSession.class);
        darInputDrlKieSessionLocal = new DARInputDrlKieSessionLocal(new FRI(SUBPATH + FRI.SLASH + "notexisting", "drl"), "");
        retrieved = DrlRuntimeHelper.execute(darInputDrlKieSessionLocal, memoryCompilerClassLoader);
        assertThat(retrieved).isNotNull().isNotPresent();
    }
}