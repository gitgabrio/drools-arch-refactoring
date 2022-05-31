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
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.RuntimeManager;
import org.kie.dar.runtimemanager.core.service.RuntimeManagerImpl;
import org.kie.foo.engine.runtime.model.DAROutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        DARInputBar toEvaluate = new DARInputBar(fri, "InputData");
        Optional<DAROutput> retrievedOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(retrievedOutput.isEmpty());
        File barFile = getFileFromFileName("DarBar.bar");
        DARResource darResourceBar = new DARFileResource(barFile);
        List<IndexFile> indexFiles = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        assertNotNull(indexFiles);
        assertEquals(1, indexFiles.size());
        retrievedOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(retrievedOutput.isPresent());
        DAROutput retrieved = retrievedOutput.get();
        assertEquals(toEvaluate.getFRI(), retrieved.getFRI());
        assertEquals(toEvaluate.getInputData(), retrieved.getOutputData());
        // TODO
//        Map<String, byte[]> compiledClasses = ((DARFinalOutputClassesContainer) retrieved.get()).getCompiledClassesMap();
//        compiledClasses.forEach(memoryCompilerClassLoader::addCode);
//        darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
//        assertTrue(darOutput.isPresent());
    }

    @Test
    void evaluateRedirectBarCompilationOnTheFly() {
        FRI fri = new FRI("redirectbar", "bar");
        DARInputBar toEvaluate = new DARInputBar(fri, "InputData");
        Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(darOutput.isEmpty());
        File barFile = getFileFromFileName("RedirectBar.bar");
        DARResource darResourceBar = new DARFileResource( barFile);
        List<IndexFile> indexFiles = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        assertNotNull(indexFiles);
        assertEquals(2, indexFiles.size());
        darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertFalse(darOutput.isEmpty());
        DAROutput retrieved = darOutput.get();
        assertEquals(toEvaluate.getFRI(), retrieved.getFRI());
        assertTrue(retrieved.getOutputData() instanceof DAROutputFoo);
        assertEquals(toEvaluate.getInputData(), ((DAROutputFoo)retrieved.getOutputData()).getOutputData());
    }

    @Test
    void evaluateExecutableBarStaticCompilation() {
        DARInputBar toEvaluate = new DARInputBar(new FRI("staticdar", "bar"), "InputData");
        Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(darOutput.isPresent());
        DAROutput retrieved = darOutput.get();
        assertEquals(toEvaluate.getFRI(), retrieved.getFRI());
        assertEquals(toEvaluate.getInputData(), retrieved.getOutputData());
    }

    @Test
    void evaluateRedirectBarStaticCompilation() {
        DARInputBar toEvaluate = new DARInputBar(new FRI("this/is/fri", "bar"), "InputData");
        Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(darOutput.isPresent());
        DAROutput retrieved = darOutput.get();
        assertEquals(toEvaluate.getFRI(), retrieved.getFRI());
        assertTrue(retrieved.getOutputData() instanceof DAROutputFoo);
        assertEquals(toEvaluate.getInputData(), ((DAROutputFoo)retrieved.getOutputData()).getOutputData());
    }

    public static File getFileFromFileName(String fileName) {
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            return Paths.get(resource.toURI()).toFile();
        } catch (Exception e) {
            throw new KieDARCommonException(String.format("Failed to retrieve %s due to %s", fileName,
                    e.getMessage()), e);
        }
    }
}
