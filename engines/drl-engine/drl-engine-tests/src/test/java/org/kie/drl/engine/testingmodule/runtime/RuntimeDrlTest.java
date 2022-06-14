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
import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.RuntimeManager;
import org.kie.dar.runtimemanager.core.service.RuntimeManagerImpl;
import org.kie.drl.engine.compilation.model.DrlFileSetResource;
import org.kie.drl.engine.runtime.kiesession.local.model.DARInputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.model.DAROutputDrlKieSessionLocal;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.drl.engine.runtime.kiesession.local.utils.DrlRuntimeHelper.SUBPATH;

class RuntimeDrlTest {

    private static RuntimeManager runtimeManager;
    private static CompilationManager compilationManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    private static final String basePath = "TestingRule";

    @BeforeAll
    static void setUp() {
        runtimeManager = new RuntimeManagerImpl();
        compilationManager = new CompilationManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    @SuppressWarnings("raw")
    void evaluateWithKieSessionLocalStaticCompilation() {
        DARInputDrlKieSessionLocal toEvaluate = new DARInputDrlKieSessionLocal(new FRI(SUBPATH + FRI.SLASH + basePath, "drl"), "");
                Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isNotNull().isPresent();
        assertThat(darOutput.get()).isInstanceOf(DAROutputDrlKieSessionLocal.class);
        DAROutputDrlKieSessionLocal retrieved = (DAROutputDrlKieSessionLocal) darOutput.get();
        assertThat(retrieved.getOutputData()).isNotNull().isInstanceOf(KieSession.class);
    }

    @Test
    @SuppressWarnings("raw")
    void evaluateWithKieSessionLocalCompilationOnTheFly() throws IOException {
        String onTheFlyPath = "OnTheFlyPath";
        DARInputDrlKieSessionLocal toEvaluate = new DARInputDrlKieSessionLocal(new FRI(SUBPATH + FRI.SLASH + onTheFlyPath, "drl"), "");
        Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isNotNull().isNotPresent();
        Set<File> files = Files.walk(Path.of("src/test/resources"))
                .map(Path::toFile)
                .filter(File::isFile)
                .collect(Collectors.toSet());
        DARResource<Set<File>> toProcess = new DrlFileSetResource(files, onTheFlyPath);
        compilationManager.processResource(toProcess, memoryCompilerClassLoader);
        darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertThat(darOutput).isNotNull().isPresent();
        assertThat(darOutput.get()).isInstanceOf(DAROutputDrlKieSessionLocal.class);
        DAROutputDrlKieSessionLocal retrieved = (DAROutputDrlKieSessionLocal) darOutput.get();
        assertThat(retrieved.getOutputData()).isNotNull().isInstanceOf(KieSession.class);
    }

}
