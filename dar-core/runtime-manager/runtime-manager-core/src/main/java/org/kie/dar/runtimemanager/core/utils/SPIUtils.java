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
package org.kie.dar.runtimemanager.core.utils;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SPIUtils {

    private SPIUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(SPIUtils.class.getName());

    private static final ServiceLoader<KieRuntimeService> loader = ServiceLoader.load(KieRuntimeService.class);

    public static Optional<KieRuntimeService> getKieRuntimeService(FRI fri, boolean refresh, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        logger.debug("getKieRuntimeService {} {}", fri, refresh);
        List<KieRuntimeService> retrieved = getKieRuntimeServices(refresh);
        return retrieved.stream().filter(service -> service.canManageInput(fri, memoryCompilerClassLoader)).findFirst();
    }

    public static List<KieRuntimeService> getKieRuntimeServices(boolean refresh) {
        logger.debug("getKieRuntimeServices {}", refresh);
        List<KieRuntimeService> toReturn = new ArrayList<>();
        Iterator<KieRuntimeService> services = getServices(refresh);
        services.forEachRemaining(toReturn::add);
        logger.debug("toReturn {} {}", toReturn, toReturn.size());
        if (logger.isTraceEnabled()) {
            toReturn.forEach(provider -> logger.trace("{}", provider));
        }
        return toReturn;
    }

    private static Iterator<KieRuntimeService> getServices(boolean refresh) {
        if (refresh) {
            loader.reload();
        }
        return loader.iterator();
    }
}
