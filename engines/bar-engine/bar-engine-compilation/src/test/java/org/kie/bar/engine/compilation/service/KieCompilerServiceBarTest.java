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
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.bar.engine.compilation.model.DARResourceIntermediateBar;
import org.kie.bar.engine.compilation.model.DARResourceBar;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.bar.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;
import static org.kie.bar.engine.compilation.TestingUtils.commonEvaluateByteCode;
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
        File fooFile = getFileFromFileName("DarBar.bar");
        DARResource toProcess = new DARResourceBar(fooFile);
        assertTrue(kieCompilerService.canManageResource(toProcess));
        toProcess = () -> "DARResource";
        assertFalse(kieCompilerService.canManageResource(toProcess));
    }

    @Test
    void processResource() {
        File fooFile = getFileFromFileName("DarBar.bar");
        DARResource toProcess = new DARResourceBar(fooFile);
        DARResourceIntermediateBar retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        assertEquals(toProcess.getFullResourceName(), retrieved.getFullResourceName());
        assertEquals("foo", retrieved.getTargetEngine());
        assertEquals(toProcess, retrieved.getContent());
        try {
            toProcess = () -> "DARResource";
            kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
            fail("Expecting KieCompilerServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieCompilerServiceException);
        }
    }
}