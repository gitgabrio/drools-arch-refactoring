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
package org.kie.bar.engine.runtime.utils;

import org.kie.bar.engine.api.model.BarResources;
import org.kie.bar.engine.runtime.model.EfestoInputBar;
import org.kie.bar.engine.runtime.model.EfestoOutputBar;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.common.api.model.GeneratedExecutableResource;
import org.kie.efesto.common.api.model.GeneratedRedirectResource;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.AbstractEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.kie.efesto.runtimemanager.api.utils.GeneratedResourceUtils.*;
import static org.kie.efesto.runtimemanager.api.utils.SPIUtils.getKieRuntimeService;

public class BarRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(BarRuntimeHelper.class.getName());


    private BarRuntimeHelper() {
    }


    public static boolean canManage(EfestoInput toEvaluate) {
        return (toEvaluate instanceof EfestoInputBar) && isPresentExecutableOrRedirect(toEvaluate.getFRI(), "bar");
    }

    public static Optional<EfestoOutputBar> execute(EfestoInputBar toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        BarResources barResources;
        try {
            barResources = loadBarResources(toEvaluate.getFRI(), memoryCompilerClassLoader);
        } catch (Exception e) {
            logger.warn("{} can not execute {}",
                    BarRuntimeHelper.class.getName(),
                    toEvaluate.getFRI());
            return Optional.empty();
        }
        try {
            return Optional.of(getEfestoOutput(barResources, toEvaluate));
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s failed to execute %s",
                    BarRuntimeHelper.class.getName(),
                    toEvaluate.getFRI()));
        }
    }

    /**
     * @param toEvaluate
     * @param memoryCompilerClassLoader
     * @return
     */
    @SuppressWarnings({"unchecked", "raw"})
    public static Optional<EfestoOutputBar> redirect(EfestoInputBar toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedRedirectResource redirectResource = getGeneratedRedirectResource(toEvaluate.getFRI(), "bar").orElse(null);
        if (redirectResource == null) {
            logger.warn("{} can not redirect {}", BarRuntimeHelper.class.getName(), toEvaluate.getFRI());
            return Optional.empty();
        }
        FRI targetFri = new FRI(redirectResource.getFri().getBasePath(), redirectResource.getTarget());
        EfestoInput<String> redirectInput = new AbstractEfestoInput<String>(targetFri, toEvaluate.getInputData()) {

        };

        Optional<KieRuntimeService> targetService = getKieRuntimeService(redirectInput, true, memoryCompilerClassLoader);
        if (targetService.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", toEvaluate.getFRI());
            return Optional.empty();
        }

        return targetService.map(service -> service.evaluateInput(redirectInput, memoryCompilerClassLoader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(o -> new EfestoOutputBar(toEvaluate.getFRI(), ((EfestoOutput<?>) o).getOutputData().toString()));
    }

    @SuppressWarnings("unchecked")
    static BarResources loadBarResources(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedExecutableResource(fri, "bar")
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        try {
            String fullBarResourcesSourceClassName = finalResource.getFullClassNames().get(0);
            final Class<? extends BarResources> aClass =
                    (Class<? extends BarResources>) memoryCompilerClassLoader.loadClass(fullBarResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    static EfestoOutputBar getEfestoOutput(BarResources barResources, EfestoInputBar darInputBar) {
        return new EfestoOutputBar(darInputBar.getFRI(), darInputBar.getInputData());
    }

}
