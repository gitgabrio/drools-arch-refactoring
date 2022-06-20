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
package org.kie.bar.engine.testingmodule.runtime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.bar.engine.runtime.model.EfestoInputBar;
import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.io.IndexFile;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.efesto.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.core.service.RuntimeManagerImpl;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeBarTest {

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
    void evaluateExecutableBarCompilationOnTheFly() {
        FRI fri = new FRI("darbar", "bar");
        EfestoInputBar toEvaluate = new EfestoInputBar(fri, "InputData");
        Optional<EfestoOutput> retrievedOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrievedOutput).isEmpty();
        File barFile = getFileFromFileName("DarBar.bar");
        EfestoResource darResourceBar = new EfestoFileResource(barFile);
        List<IndexFile> indexFiles = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        assertThat(indexFiles).isNotNull().hasSize(1);
        retrievedOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(retrievedOutput).isPresent();
        EfestoOutput retrieved = retrievedOutput.get();
        assertThat(retrieved.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(retrieved.getOutputData()).isEqualTo(toEvaluate.getInputData());
    }

    @Test
    void evaluateRedirectBarCompilationOnTheFly() {
        FRI fri = new FRI("redirectbar", "bar");
        EfestoInputBar toEvaluate = new EfestoInputBar(fri, "InputData");
        Optional<EfestoOutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isNotNull().isNotPresent();
        File barFile = getFileFromFileName("RedirectBar.bar");
        EfestoResource darResourceBar = new EfestoFileResource(barFile);
        List<IndexFile> indexFiles = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        assertThat(indexFiles).isNotNull().hasSize(2);
        darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isNotEmpty();
        EfestoOutput retrieved = darOutput.get();
        assertThat(retrieved.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(retrieved.getOutputData()).isInstanceOf(String.class);
        assertThat(retrieved.getOutputData()).isEqualTo(toEvaluate.getInputData());
    }

    @Test
    void evaluateExecutableBarStaticCompilation() {
        EfestoInputBar toEvaluate = new EfestoInputBar(new FRI("staticdar", "bar"), "InputData");
        Optional<EfestoOutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isPresent();
        EfestoOutput retrieved = darOutput.get();
        assertThat(retrieved.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(retrieved.getOutputData()).isEqualTo(toEvaluate.getInputData());
    }

    @Test
    void evaluateRedirectBarStaticCompilation() {
        EfestoInputBar toEvaluate = new EfestoInputBar(new FRI("this/is/fri", "bar"), "InputData");
        Optional<EfestoOutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isPresent();
        EfestoOutput retrieved = darOutput.get();
        assertThat(retrieved.getFRI()).isEqualTo(toEvaluate.getFRI());
        assertThat(retrieved.getOutputData()).isInstanceOf(String.class).isEqualTo(toEvaluate.getInputData());
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
