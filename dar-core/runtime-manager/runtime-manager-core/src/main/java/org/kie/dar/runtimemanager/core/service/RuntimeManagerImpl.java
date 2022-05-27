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
package org.kie.dar.runtimemanager.core.service;

import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.dar.runtimemanager.api.service.RuntimeManager;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.kie.dar.runtimemanager.core.utils.SPIUtils.getKieRuntimeService;

public class RuntimeManagerImpl implements RuntimeManager {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeManagerImpl.class.getName());

    @Override
    public Optional<DAROutput> evaluateInput(DARInput toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        Optional<KieRuntimeService> retrieved = getKieRuntimeService(toEvaluate.getFRI(), true, memoryCompilerClassLoader);
        if (retrieved.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", toEvaluate.getFRI());
        }
        return retrieved.map(service -> service.evaluateInput(toEvaluate, memoryCompilerClassLoader));
    }

    @Override
    public List<DAROutput> evaluateInputs(List<DARInput> toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        return toEvaluate.stream().map(darInput -> evaluateInput(darInput, memoryCompilerClassLoader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
