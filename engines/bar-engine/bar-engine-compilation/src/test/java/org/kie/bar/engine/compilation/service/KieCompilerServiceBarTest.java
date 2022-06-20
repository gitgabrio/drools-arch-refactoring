package org.kie.bar.engine.compilation.service;/*
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
import org.kie.bar.engine.compilation.model.EfestoCallableOutputBar;
import org.kie.bar.engine.compilation.model.EfestoRedirectOutputBar;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.bar.engine.compilation.TestingUtils.getFileFromFileName;

class KieCompilerServiceBarTest {

    private static KieCompilerService kieCompilerService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieCompilerService = new KieCompilerServiceBar();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageResource() {
        File barFile = getFileFromFileName("DarBar.bar");
        EfestoResource toProcess = new EfestoFileResource(barFile);
        assertThat(kieCompilerService.canManageResource(toProcess)).isTrue();
        barFile = getFileFromFileName("RedirectBar.bar");
        toProcess = new EfestoFileResource(barFile);
        assertThat(kieCompilerService.canManageResource(toProcess)).isTrue();
        toProcess = () -> "EfestoRedirectOutput";
        assertThat(kieCompilerService.canManageResource(toProcess)).isFalse();
    }

    @Test
    void processResource() {
        File barFile = getFileFromFileName("DarBar.bar");
        EfestoResource toProcess = new EfestoFileResource(barFile);
        List<EfestoCompilationOutput> listRetrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertThat(listRetrieved).isNotNull().hasSize(1);
        EfestoCompilationOutput retrieved = listRetrieved.get(0);
        assertThat(retrieved).isInstanceOf(EfestoCallableOutputBar.class);

        barFile = getFileFromFileName("RedirectBar.bar");
        toProcess = new EfestoFileResource(barFile);
        listRetrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertThat(listRetrieved).isNotNull().hasSize(1);
        retrieved = listRetrieved.get(0);
        assertThat(retrieved).isNotNull().isInstanceOf(EfestoRedirectOutputBar.class);
        assertThat(((EfestoRedirectOutputBar) retrieved).getTargetEngine()).isEqualTo("foo");

        try {
            toProcess = () -> "EfestoRedirectOutput";
            kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
            fail("Expecting KieCompilerServiceException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(KieCompilerServiceException.class);
        }
    }
}