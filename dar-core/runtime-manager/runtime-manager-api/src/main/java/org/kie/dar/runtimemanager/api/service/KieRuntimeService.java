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
package org.kie.dar.runtimemanager.api.service;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.Serializable;

/**
 * The compilation-related interface to be implemented by engine-plugin.
 * It will be looked for with SPI, so each engine should declare that implementation inside
 * <code>src/main/resources/META-INF/services/org.kie.dar.runtimemanager.api.service.KieRuntimeService</code> file
 */
public interface KieRuntimeService<K, T extends DARInput<K>, E extends DAROutput> {


    /**
     * Every engine is responsible to verify if it can evaluate a result with the resource of the given <code>FRI</code>
     *
     * @param fri
     * @param memoryCompilerClassLoader
     * @return
     */
    boolean canManageInput(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader);

    /**
     * Produce one <code>DAROutput</code> from the given <code>DARInput</code>
     *
     * @param toEvaluate
     * @param memoryCompilerClassLoader
     * @return
     */
    E evaluateInput(T toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader);

}
