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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.bar.engine.compilation.model.DARIntermediateOutputBar;
import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedClassResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.dar.compilationmanager.core.service.CompilationManagerImpl;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;

class CompileBarTest {

    private static CompilationManager compilationManager;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        compilationManager = new CompilationManagerImpl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(CompilationManager.class.getClassLoader());
    }


    @BeforeEach
    public void init() {
        try {
            getFileFromFileName("IndexFile.foo_json").delete();
        } catch (KieDARCommonException e) {
            // Ignore
        }
        try {
            getFileFromFileName("IndexFile.bar_json").delete();
        } catch (KieDARCommonException e) {
            // Ignore
        }
    }

    @Test
    void compileRedirectBar() {
        File barFile = getFileFromFileName("DarBar.bar");
        DARResource darResourceBar = new DARFileResource( barFile);
        List<IndexFile> retrieved = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.stream().anyMatch(ind -> ind.getName().equals("IndexFile.bar_json")));
        assertTrue(retrieved.stream().anyMatch(ind -> ind.getName().equals("IndexFile.foo_json")));


        // TODO
//        assertTrue(darCompilationOutput.isPresent());
//        DARCompilationOutput retrieved = darCompilationOutput.get();
//        assertTrue(retrieved instanceof DARFinalOutputFoo);
    }

    @Test
    void compileExecuteBar() throws IOException {
        FRI fri = new FRI("bar/darbar", "bar");
        File fooFile = getFileFromFileName("DarBar.bar");
        DARIntermediateOutputBar darResourceBar = new DARIntermediateOutputBar(fri, fooFile);
        List<IndexFile> retrieved = compilationManager.processResource(darResourceBar, memoryCompilerClassLoader);
        int involvedEngines = 1;
        assertEquals(involvedEngines, retrieved.size());
        assertEquals("foo", retrieved.get(0).getModel());
        GeneratedResources generatedResources = getGeneratedResourcesObject(retrieved.get(0));
        List<String> generatedClasses = generatedResources.stream()
                .filter(GeneratedClassResource.class::isInstance)
                .map(GeneratedClassResource.class::cast)
                .map(GeneratedClassResource::getFullClassName)
                .collect(Collectors.toList());
        for (String generatedClass : generatedClasses) {
            try {
                Class<?> loadedClass = memoryCompilerClassLoader.loadClass(generatedClass);
                System.out.println(loadedClass);
            } catch (Exception e) {
                fail("Failed to load " + generatedClass);
            }
        }
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
