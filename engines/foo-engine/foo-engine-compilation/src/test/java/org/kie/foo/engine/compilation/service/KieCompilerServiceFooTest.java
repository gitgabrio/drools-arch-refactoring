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
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.foo.engine.compilation.model.DARProcessedFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.foo.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;
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
        DARResource toProcess = getDARResourceFileContainer(getFileFromFileName("DarFoo.foo"));
        assertTrue(kieCompilerService.canManageResource(toProcess));
        toProcess = getDARResourceIntermediate();
        assertTrue(kieCompilerService.canManageResource(toProcess));
        toProcess = getDARResource();
        assertFalse(kieCompilerService.canManageResource(toProcess));
    }

    @Test
    void processResource() {
        DARResource toProcess = getDARResourceFileContainer(getFileFromFileName("DarFoo.foo"));
        DARProcessedFoo retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        Map<String, byte[]> retrievedByteCode = retrieved.getCompiledClassesMap();
        String fullClassName = FOO_MODEL_PACKAGE_NAME + "." + getSanitizedClassName(toProcess.getFullResourceName());
        commonEvaluateByteCode(retrievedByteCode, fullClassName, memoryCompilerClassLoader);
        fullClassName += "Resources";
        commonEvaluateByteCode(retrievedByteCode, fullClassName, memoryCompilerClassLoader);
        toProcess = getDARResourceIntermediate();
        retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        retrievedByteCode = retrieved.getCompiledClassesMap();
        fullClassName = FOO_MODEL_PACKAGE_NAME + "." + getSanitizedClassName(toProcess.getFullResourceName());
        commonEvaluateByteCode(retrievedByteCode, fullClassName, memoryCompilerClassLoader);
        fullClassName += "Resources";
        commonEvaluateByteCode(retrievedByteCode, fullClassName, memoryCompilerClassLoader);
        try {
            toProcess = getDARResource();
            kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
            fail("Expecting KieCompilerServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieCompilerServiceException);
        }
    }

}