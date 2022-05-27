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
package org.kie.foo.engine.testingmodule.runtime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.RuntimeManager;
import org.kie.dar.runtimemanager.core.service.RuntimeManagerImpl;
import org.kie.foo.engine.compilation.model.DARFinalOutputFoo;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;

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
        DARInputFoo toEvaluate = new DARInputFoo("DarFoofoo", "InputData");
        Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(darOutput.isEmpty());
        File fooFile = getFileFromFileName("DarFoo.foo");
        DARResource darResourceFileFoo = new DARFileResource(fooFile);
        List<IndexFile> indexFiles = compilationManager.processResource(darResourceFileFoo, memoryCompilerClassLoader);
        assertEquals(1, indexFiles.size());
        IndexFile retrieved = indexFiles.get(0);
        assertTrue(retrieved.exists());
        GeneratedResources generatedResources = getGeneratedResourcesObject(retrieved);
        System.out.println(generatedResources);
        retrieved.delete();
        // TODO
//        Map<String, byte[]> compiledClasses = ((DARFinalOutputFoo) darProcessed.get()).getCompiledClassesMap();
//        compiledClasses.forEach(memoryCompilerClassLoader::addCode);
//        darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
//        assertTrue(darOutput.isPresent());
    }

    @Test
    void evaluateFooStaticCompilation() {
        DARInputFoo toEvaluate = new DARInputFoo("DarFooStaticfoo", "InputData");
        Optional<DAROutput> darOutput = runtimeManager.evaluateInput(toEvaluate, memoryCompilerClassLoader);
        assertTrue(darOutput.isPresent());
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
