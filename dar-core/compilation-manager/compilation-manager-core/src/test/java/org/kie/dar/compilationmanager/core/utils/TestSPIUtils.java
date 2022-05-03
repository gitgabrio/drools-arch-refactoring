package org.kie.dar.compilationmanager.core.utils;/*
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
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.dar.compilationmanager.core.mocks.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestSPIUtils {

    private static final List<Class<? extends KieCompilerService>> KIE_COMPILER_SERVICES = Arrays.asList(MockKieCompilerServiceAB.class, MockKieCompilerServiceC.class);

    @Test
    void getKieCompilerService() {
        Optional<KieCompilerService> retrieved = SPIUtils.getKieCompilerService(new MockDARResourceA(), true);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof MockKieCompilerServiceAB);
        retrieved = SPIUtils.getKieCompilerService(new MockDARResourceB(), true);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof MockKieCompilerServiceAB);
        retrieved = SPIUtils.getKieCompilerService(new MockDARResourceC(), true);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get() instanceof MockKieCompilerServiceC);
        retrieved = SPIUtils.getKieCompilerService(new MockDARResourceD(), true);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getKieCompilerServices() {
        List<KieCompilerService> retrieved = SPIUtils.getKieCompilerServices(true);
        assertNotNull(retrieved);
        assertEquals(KIE_COMPILER_SERVICES.size(), retrieved.size());
    }
}