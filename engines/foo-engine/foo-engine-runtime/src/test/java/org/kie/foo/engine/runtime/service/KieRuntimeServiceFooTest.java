package org.kie.foo.engine.runtime.service;/*
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
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;

import static org.junit.jupiter.api.Assertions.*;

class KieRuntimeServiceFooTest {

    private static KieRuntimeService kieRuntimeService;

    @BeforeAll
    static void setUp() {
        kieRuntimeService = new KieRuntimeServiceFoo();
    }

    @Test
    void canManageResource() {
        assertTrue(kieRuntimeService.canManageInput("DarFoo"));
        assertFalse(kieRuntimeService.canManageInput("DarNotFoo"));
    }

    @Test
    void evaluateInputExistingFooResources() {
        DARInputFoo toEvaluate = new DARInputFoo("DarFoo", "InputData");
        DAROutputFoo retrieved = kieRuntimeService.evaluateInput(toEvaluate);
        assertNotNull(retrieved);
        assertEquals(toEvaluate.getFullResourceName(), retrieved.getFullResourceName());
        assertEquals(toEvaluate.getInputData(), retrieved.getOutputData());
    }

    @Test
    void evaluateInputNotExistingFooResources() {
        try {
            DARInputFoo toEvaluate = new DARInputFoo("DarNotFoo", "InputData");
            kieRuntimeService.evaluateInput(toEvaluate);
            fail("Expecting KieRuntimeServiceException");
        } catch (Exception e) {
            assertTrue(e instanceof KieRuntimeServiceException);
        }
    }

}