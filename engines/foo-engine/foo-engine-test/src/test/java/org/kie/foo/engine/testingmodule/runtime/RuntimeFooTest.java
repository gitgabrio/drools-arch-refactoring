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
package org.kie.drl.engine.testingmodule.runtime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.io.IndexFile;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.common.api.model.GeneratedResources;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.core.service.RuntimeManagerImpl;
import org.kie.foo.engine.runtime.model.EfestoInputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.efesto.common.api.utils.JSONUtils.getGeneratedResourcesObject;

class RuntimeFooTest {

    private static RuntimeManager runtimeManager;
    private static CompilationManager compilationManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        runtimeManager = new RuntimeManagerImpl();
        compilationManager = new CompilationManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void evaluateFooCompilationOnTheFly() throws IOException {
        EfestoInputFoo toEvaluate = new EfestoInputFoo(new FRI("efesto", "foo"), "InputData");
        Optional<EfestoOutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput.isEmpty()).isTrue();
        File fooFile = getFileFromFileName("DarFoo.foo");
        EfestoResource darResourceFileFoo = new EfestoFileResource(fooFile);
        List<IndexFile> indexFiles = compilationManager.processResource(darResourceFileFoo, memoryCompilerClassLoader);
        assertThat(indexFiles.size()).isEqualTo(1);
        IndexFile retrieved = indexFiles.get(0);
        assertThat(retrieved.exists()).isTrue();
        GeneratedResources generatedResources = getGeneratedResourcesObject(retrieved);
        retrieved.delete();
    }

    @Test
    void evaluateFooStaticCompilation() {
        EfestoInputFoo toEvaluate = new EfestoInputFoo(new FRI("staticdar", "foo"), "InputData");
        Optional<EfestoOutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput.isPresent()).isTrue();
    }

    public static File getFileFromFileName(String fileName) {
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            return Paths.get(resource.toURI()).toFile();
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to retrieve %s due to %s", fileName,
                    e.getMessage()), e);
        }
    }
}
