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
package org.kie.bar.engine.runtime.service;

import org.kie.bar.engine.api.model.BarResources;
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.bar.engine.runtime.utils.BarRuntimeHelper.*;

public class KieRuntimeServiceBar implements KieRuntimeService {

    private static final Logger logger = LoggerFactory.getLogger(KieRuntimeServiceBar.class.getName());


    @Override
    public boolean canManageInput(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        return canManage(fri);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DARInput, E extends DAROutput> E evaluateInput(T toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        try {
            BarResources fooResources = loadBarResources(toEvaluate.getFRI(), memoryCompilerClassLoader);
            return (E) getDAROutput(fooResources, (DARInputBar) toEvaluate);
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s can not evaluate %s",
                    this.getClass().getName(),
                    toEvaluate.getFRI()));
        }

    }
}
