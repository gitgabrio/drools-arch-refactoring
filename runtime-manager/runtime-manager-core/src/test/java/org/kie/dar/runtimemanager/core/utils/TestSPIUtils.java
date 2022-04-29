package org.kie.dar.runtimemanager.core.utils;/*
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

import org.junit.jupiter.api.Test;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.dar.runtimemanager.core.mocks.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestSPIUtils {

    private static final List<Class<? extends KieRuntimeService>> KIE_RUNTIME_SERVICES = Arrays.asList(MockKieRuntimeServiceAB.class, MockKieRuntimeServiceC.class);

    @Test
    void getKieRuntimeService() {
        Optional<KieRuntimeService> retrieved = SPIUtils.getKieRuntimeService(MockDARInputA.class.getSimpleName(), true);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof MockKieRuntimeServiceAB);
        retrieved = SPIUtils.getKieRuntimeService(MockDARInputB.class.getSimpleName(), true);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof MockKieRuntimeServiceAB);
        retrieved = SPIUtils.getKieRuntimeService(MockDARInputC.class.getSimpleName(), true);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof MockKieRuntimeServiceC);
        retrieved = SPIUtils.getKieRuntimeService(MockDARInputD.class.getSimpleName(), true);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getKieRuntimeServices() {
        List<KieRuntimeService> retrieved = SPIUtils.getKieRuntimeServices(true);
        assertNotNull(retrieved);
        assertEquals(KIE_RUNTIME_SERVICES.size(), retrieved.size());
    }
}