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
package org.kie.drl.engine.runtime.kiesession.local.service;

import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.drl.engine.runtime.kiesession.local.model.DARInputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.model.DAROutputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.utils.DrlRuntimeHelper;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


public class KieRuntimeServiceDrlKieSessionLocal implements KieRuntimeService<String, KieSession, DARInputDrlKieSessionLocal, DAROutputDrlKieSessionLocal> {

    private static final Logger logger = LoggerFactory.getLogger(KieRuntimeServiceDrlKieSessionLocal.class.getName());


    @Override
    public boolean canManageInput(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        return DrlRuntimeHelper.canManage(fri);
    }

    @Override
    public Optional<DAROutputDrlKieSessionLocal> evaluateInput(DARInputDrlKieSessionLocal toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        return canManageInput(toEvaluate.getFRI(), memoryCompilerClassLoader) ? DrlRuntimeHelper.execute(toEvaluate, memoryCompilerClassLoader) : Optional.empty();
    }
}
