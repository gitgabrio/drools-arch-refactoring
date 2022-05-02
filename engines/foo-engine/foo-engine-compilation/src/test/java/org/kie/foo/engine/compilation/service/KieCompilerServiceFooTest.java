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
import org.kie.foo.engine.compilation.model.DARResourceFoo;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.foo.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;
import static org.kie.foo.engine.compilation.TestingUtils.commonEvaluateByteCode;

class KieCompilerServiceFooTest {

    private static KieCompilerService kieCompilerService;

    @BeforeAll
    static void setUp() {
        kieCompilerService = new KieCompilerServiceFoo();
    }

    @Test
    void canManageResource() {
        DARResource toProcess = new DARResourceFoo("DarResourceFoo");
        assertTrue(kieCompilerService.canManageResource(toProcess));
        toProcess = () -> "DARResource";
        assertFalse(kieCompilerService.canManageResource(toProcess));
    }

    @Test
    void processResource() {
        DARResource toProcess = new DARResourceFoo("fullResourceName");
        DARProcessedFoo retrieved = kieCompilerService.processResource(toProcess);
        assertNotNull(retrieved);
        Map<String, byte[]> retrievedByteCode = retrieved.getCompiledClassesMap();
        String fullClassName = FOO_MODEL_PACKAGE_NAME + "." + getSanitizedClassName(toProcess.getFullResourceName());
        commonEvaluateByteCode(retrievedByteCode, fullClassName);
        fullClassName += "Resources";
        commonEvaluateByteCode(retrievedByteCode, fullClassName);
        try {
            toProcess = () -> "DARResource";
            kieCompilerService.processResource(toProcess);
            fail("Expecting KieCompilerServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieCompilerServiceException);
        }
    }
}