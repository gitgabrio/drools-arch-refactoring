/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.pmml.evaluator.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.drools.core.definitions.InternalKnowledgePackage;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.dar.runtimemanager.api.context.RuntimePackageContainer;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.evaluator.api.container.PMMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnowledgeBaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseUtils.class);

    private KnowledgeBaseUtils() {
        // Avoid instantiation
    }

    public static List<KiePMMLModel> getModels(final KieMemoryCompiler.MemoryCompilerClassLoader classLoader) {
        KiePMMLModel.class.get


        List<KiePMMLModel> models = new ArrayList<>();
        KieMemoryCompiler.MemoryCompilerClassLoader.getSystemClassLoader();
        classLoader.
        runtimePackageContainer.getRuntimePackages().forEach(kpkg -> {
            PMMLPackage pmmlPackage = (PMMLPackage) kpkg.getResourceTypePackages().get(ResourceType.PMML);
            if (pmmlPackage != null) {
                models.addAll(pmmlPackage.getAllModels().values());
            }
        });
        return models;
    }

    public static Optional<KiePMMLModel> getModel(final RuntimePackageContainer runtimePackageContainer, String modelName) {
        logger.trace("getModels {} {}", runtimePackageContainer, modelName);
        return getModels(runtimePackageContainer)
                .stream()
                .filter(model -> Objects.equals(modelName, model.getName()))
                .findFirst();
    }
}
