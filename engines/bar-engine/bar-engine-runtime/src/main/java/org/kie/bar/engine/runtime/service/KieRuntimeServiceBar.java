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

import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.bar.engine.runtime.model.DAROutputBar;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

import static org.kie.bar.engine.runtime.utils.BarRuntimeHelper.*;

public class KieRuntimeServiceBar implements KieRuntimeService<String, String, DARInputBar, DAROutputBar> {

    private static final Logger logger = LoggerFactory.getLogger(KieRuntimeServiceBar.class.getName());


    @Override
    public boolean canManageInput(DARInput toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        return canManage(toEvaluate);
    }

    @Override
    public Optional<DAROutputBar> evaluateInput(DARInputBar toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        return Stream.of(execute(toEvaluate, memoryCompilerClassLoader),
                        redirect(toEvaluate, memoryCompilerClassLoader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
