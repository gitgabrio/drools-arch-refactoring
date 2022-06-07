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
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.bar.engine.runtime.model.DAROutputBar;
import org.kie.bar.engine.runtime.service.KieRuntimeServiceBar;
import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.common.api.model.GeneratedRedirectResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;
import static org.kie.dar.runtimemanager.api.utils.SPIUtils.getKieRuntimeService;

public class BarRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(BarRuntimeHelper.class.getName());


    private BarRuntimeHelper() {
    }


    public static boolean canManage(FRI fri) {
        return Stream.of(getGeneratedExecutableResource(fri), getGeneratedRedirectResource(fri))
                .anyMatch(Optional::isPresent);
    }

    public static Optional<DAROutputBar> execute(DARInputBar toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
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
            return Optional.of(getDAROutput(barResources, toEvaluate));
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s failed to execute %s",
                    BarRuntimeHelper.class.getName(),
                    toEvaluate.getFRI()));
        }
    }

    public static Optional<DAROutputBar> redirect(DARInputBar toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedRedirectResource redirectResource = getGeneratedRedirectResource(toEvaluate.getFRI()).orElse(null);
        if (redirectResource == null) {
            logger.warn("{} can not redirect {}", BarRuntimeHelper.class.getName(), toEvaluate.getFRI());
            return Optional.empty();
        }
        FRI targetFri = new FRI(redirectResource.getFri().getBasePath(), redirectResource.getTarget());
        DARInputBar redirectInput = new DARInputBar(targetFri, toEvaluate.getInputData());

        Optional<KieRuntimeService> targetService = getKieRuntimeService(redirectInput.getFRI(), true, memoryCompilerClassLoader);
        if (targetService.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", toEvaluate.getFRI());
            return Optional.empty();
        }
        KieRuntimeServiceBar service = (KieRuntimeServiceBar)targetService.get();
        return Optional.of(service.evaluateInput(redirectInput, memoryCompilerClassLoader));

    }

    static BarResources loadBarResources(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedExecutableResource(fri)
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        String fullBarResourcesSourceClassName = finalResource.getFullClassName();
        try {
            final Class<? extends BarResources> aClass =
                    (Class<? extends BarResources>) memoryCompilerClassLoader.loadClass(fullBarResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    static DAROutputBar getDAROutput(BarResources barResources, DARInputBar darInputBar) {
        return new DAROutputBar(darInputBar.getFRI(), darInputBar.getInputData());
    }

    static Optional<DAROutput> evaluateRedirectInput(DARInputBar toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        Optional<KieRuntimeService> retrieved = getKieRuntimeService(toEvaluate.getFRI(), true, memoryCompilerClassLoader);
        if (retrieved.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", toEvaluate.getFRI());
        }
        return retrieved.map(service -> service.evaluateInput(toEvaluate, memoryCompilerClassLoader));
    }

    static Optional<IndexFile> getIndexFile() {
        IndexFile toSearch = new IndexFile("bar");
        File existingFile;
        try {
            existingFile = getFileFromFileName(toSearch.getName());
            toSearch = new IndexFile(existingFile);
            logger.debug("IndexFile {} exists", toSearch.getName());
            return Optional.of(toSearch);
        } catch (KieDARCommonException e) {
            logger.debug("IndexFile {} does not exists.", toSearch.getName());
            return Optional.empty();
        }
    }

    static Optional<GeneratedExecutableResource> getGeneratedExecutableResource(FRI fri) {
        return getIndexFile().map(indexFile -> {
            try {
                GeneratedResources generatedResources = getGeneratedResourcesObject(indexFile);
                return generatedResources.stream()
                        .filter(generatedResource -> generatedResource instanceof GeneratedExecutableResource &&
                                ((GeneratedExecutableResource) generatedResource).getFri().equals(fri))
                        .findFirst()
                        .map(GeneratedExecutableResource.class::cast)
                        .orElse(null);
            } catch (IOException e) {
                logger.debug("Failed to read GeneratedResources from {}.", indexFile.getName(), e);
                return null;
            }
        });
    }

    static Optional<GeneratedRedirectResource> getGeneratedRedirectResource(FRI fri) {
        return getIndexFile().map(indexFile -> {
            try {
                GeneratedResources generatedResources = getGeneratedResourcesObject(indexFile);
                return generatedResources.stream()
                        .filter(generatedResource -> generatedResource instanceof GeneratedRedirectResource &&
                                ((GeneratedRedirectResource) generatedResource).getFri().equals(fri))
                        .findFirst()
                        .map(GeneratedRedirectResource.class::cast)
                        .orElse(null);
            } catch (IOException e) {
                logger.debug("Failed to read GeneratedResources from {}.", indexFile.getName(), e);
                return null;
            }
        });
    }
}
