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
package org.kie.foo.engine.runtime.service;

import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.foo.engine.api.model.FooResources;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.foo.engine.runtime.utils.FooRuntimeHelper.getDAROutput;
import static org.kie.foo.engine.runtime.utils.FooRuntimeHelper.loadFooResources;

public class KieRuntimeServiceFoo implements KieRuntimeService {

    private static final Logger logger = LoggerFactory.getLogger(KieRuntimeServiceFoo.class.getName());


    @Override
    public boolean canManageInput(String fullResourceIdentifier, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        try {
            loadFooResources(fullResourceIdentifier, memoryCompilerClassLoader);
            return true;
        } catch (Exception e) {
            logger.warn(String.format("Failed to find resource %s due to: %s", fullResourceIdentifier, e.getMessage()));
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DARInput, E extends DAROutput> E evaluateInput(T toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        try {
            FooResources fooResources = loadFooResources(toEvaluate.getFullResourceIdentifier(), memoryCompilerClassLoader);
            DARInputFoo darInputFoo = new DARInputFoo(toEvaluate.getFullResourceIdentifier(), toEvaluate.getInputData());
            return (E) getDAROutput(fooResources, darInputFoo);
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s can not evaluate %s",
                    this.getClass().getName(),
                    toEvaluate.getFullResourceIdentifier()));
        }

    }
}
