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
import org.kie.dar.compilationmanager.api.utils.SPIUtils;
import org.kie.dar.compilationmanager.core.mocks.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TestSPIUtils {

    private static final List<Class<? extends KieCompilerService>> KIE_COMPILER_SERVICES = Arrays.asList(MockKieCompilerServiceAB.class, MockKieCompilerServiceC.class);

    @Test
    void getKieCompilerService() {
        Optional<KieCompilerService> retrieved = SPIUtils.getKieCompilerService(new MockDARRedirectOutputA(), true);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get() instanceof MockKieCompilerServiceAB).isTrue();
        retrieved = SPIUtils.getKieCompilerService(new MockDARRedirectOutputB(), true);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get() instanceof MockKieCompilerServiceAB).isTrue();
        retrieved = SPIUtils.getKieCompilerService(new MockDARRedirectOutputC(), true);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get() instanceof MockKieCompilerServiceC).isTrue();
        retrieved = SPIUtils.getKieCompilerService(new MockDARRedirectOutputD(), true);
        assertThat(retrieved.isEmpty()).isTrue();
    }

    @Test
    void getKieCompilerServices() {
        List<KieCompilerService> retrieved = SPIUtils.getKieCompilerServices(true);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.size()).isEqualTo(KIE_COMPILER_SERVICES.size());
    }
}