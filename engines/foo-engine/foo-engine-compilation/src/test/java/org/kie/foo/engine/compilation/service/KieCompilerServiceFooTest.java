package org.kie.foo.engine.compilation.service;/*
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
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.foo.engine.compilation.model.EfestoCallableOutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.foo.engine.compilation.TestingUtils.*;

class KieCompilerServiceFooTest {

    private static KieCompilerService kieCompilerService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieCompilerService = new KieCompilerServiceFoo();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void canManageResource() {
        EfestoResource toProcess = getEfestoFileResource(getFileFromFileName("DarFoo.foo"));
        assertThat(kieCompilerService.canManageResource(toProcess)).isTrue();
        toProcess = getEfestoResourceIntermediate();
        assertThat(kieCompilerService.canManageResource(toProcess)).isTrue();
        toProcess = getEfestoResource();
        assertThat(kieCompilerService.canManageResource(toProcess)).isFalse();
    }

    @Test
    void processResource() {
        EfestoResource toProcess = getEfestoFileResource(getFileFromFileName("DarFoo.foo"));
        List<EfestoCallableOutputFoo> listRetrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertThat(listRetrieved).isNotNull().hasSize(1);
        EfestoCallableOutputFoo retrieved = listRetrieved.get(0);
        Map<String, byte[]> retrievedByteCode1 = retrieved.getCompiledClassesMap();
        retrievedByteCode1.forEach((fullClassName, bytes) -> commonEvaluateByteCode(retrievedByteCode1, fullClassName, memoryCompilerClassLoader));

        toProcess = getEfestoResourceIntermediate();
        listRetrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertThat(listRetrieved).isNotNull().hasSize(1);
        retrieved = listRetrieved.get(0);
        Map<String, byte[]> retrievedByteCode2 = retrieved.getCompiledClassesMap();
        retrievedByteCode2.forEach((fullClassName, bytes) -> commonEvaluateByteCode(retrievedByteCode2, fullClassName, memoryCompilerClassLoader));

        try {
            toProcess = getEfestoResource();
            kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
            fail("Expecting KieCompilerServiceException");
        } catch (Exception e) {
            assertThat(e instanceof KieCompilerServiceException).isTrue();
        }
    }

}