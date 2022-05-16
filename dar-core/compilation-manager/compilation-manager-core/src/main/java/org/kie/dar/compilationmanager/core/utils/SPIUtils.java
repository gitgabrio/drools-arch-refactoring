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
package org.kie.dar.compilationmanager.core.utils;

import org.kie.dar.compilationmanager.api.model.DARIntermediateOutput;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SPIUtils {

    private SPIUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(SPIUtils.class.getName());

    private static final ServiceLoader<KieCompilerService> loader = ServiceLoader.load(KieCompilerService.class);

    public static Optional<KieCompilerService> getKieCompilerService(DARResource resource, boolean refresh) {
        logger.debug("getKieCompilerService {} {}", resource, refresh);
        List<KieCompilerService> retrieved = getKieCompilerServices(refresh);
        return retrieved.stream().filter(service -> service.canManageResource(resource)).findFirst();
    }

    public static List<KieCompilerService> getKieCompilerServices(boolean refresh) {
        logger.debug("getKieCompilerServices {}", refresh);
        List<KieCompilerService> toReturn = new ArrayList<>();
        Iterator<KieCompilerService> services = getServices(refresh);
        services.forEachRemaining(toReturn::add);
        logger.debug("toReturn {} {}", toReturn, toReturn.size());
        if (logger.isTraceEnabled()) {
            toReturn.forEach(provider -> logger.trace("{}", provider));
        }
        return toReturn;
    }

    private static Iterator<KieCompilerService> getServices(boolean refresh) {
        if (refresh) {
            loader.reload();
        }
        return loader.iterator();
    }
}
