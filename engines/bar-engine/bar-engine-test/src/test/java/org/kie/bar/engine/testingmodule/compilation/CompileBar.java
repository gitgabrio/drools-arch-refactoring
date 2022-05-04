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
package org.kie.bar.engine.testingmodule.compilation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dar.common.exceptions.KieDARCommonException;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARProcessed;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.bar.engine.compilation.model.DARProcessedBar;
import org.kie.bar.engine.compilation.model.DARResourceBar;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompileBarTest {

    private static CompilationManager compilationManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        compilationManager = new CompilationManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(CompilationManager.class.getClassLoader());
    }

    @Test
    void compileBar() {
        File fooFile = getFileFromFileName("DarBar.bar");
        DARResourceBar darResourceBar = new DARResourceBar(fooFile);
        Optional<DARCompilationOutput> darProcessed = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        assertTrue(darProcessed.isPresent());
        DARCompilationOutput retrieved = darProcessed.get();
        assertTrue(retrieved instanceof DARProcessedBar);
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
