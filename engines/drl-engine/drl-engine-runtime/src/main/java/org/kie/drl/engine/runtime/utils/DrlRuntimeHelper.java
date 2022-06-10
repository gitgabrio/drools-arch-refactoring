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
package org.kie.drl.engine.runtime.utils;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.model.Model;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.common.api.model.GeneratedRedirectResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.drl.engine.runtime.model.DARInputDrl;
import org.kie.drl.engine.runtime.model.DAROutputDrl;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;

public class DrlRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(DrlRuntimeHelper.class.getName());


    private DrlRuntimeHelper() {
    }


    public static boolean canManage(FRI fri) {
        return Stream.of(getGeneratedExecutableResource(fri), getGeneratedRedirectResource(fri))
                .anyMatch(Optional::isPresent);
    }

    public static Optional<DAROutputDrl> execute(DARInputDrl toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        KieSession kieSession;
        try {
            kieSession = loaKieSession(toEvaluate.getFRI(), memoryCompilerClassLoader);
        } catch (Exception e) {
            logger.warn("{} can not execute {}",
                    DrlRuntimeHelper.class.getName(),
                    toEvaluate.getFRI());
            return Optional.empty();
        }
        try {
            return Optional.of(getDAROutput(kieSession, toEvaluate));
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s failed to execute %s",
                    DrlRuntimeHelper.class.getName(),
                    toEvaluate.getFRI()));
        }
    }

    static DAROutputDrl getDAROutput(KieSession kieSession, DARInputDrl darInputDrl) {
        // TODO {mfusco} Read the "input" for rule execution and put it in session
        //
//        List<LoanApplication> approvedApplications = new ArrayList<>();
//
//        kieSession.setGlobal("approvedApplications", approvedApplications);
//        kieSession.setGlobal("maxAmount", loanAppDto.getMaxAmount());
//        loanAppDto.getLoanApplications().forEach(session::insert);
//
//        kieSession.fireAllRules();
//        kieSession.dispose();

        // TODO {mfusco} Get the "output" from rule execution and put it returned DAROutputDrl
        return new DAROutputDrl(darInputDrl.getFRI(), darInputDrl.getInputData());
    }


    @SuppressWarnings("unchecked")
    static KieSession loaKieSession(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedExecutableResource(fri)
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        List<Model> models = finalResource.getFullClassNames().stream().map(className -> loadModel(className, memoryCompilerClassLoader)).collect(Collectors.toList());
        KnowledgeBuilderImpl temp = new KnowledgeBuilderImpl(KieBaseBuilder.createKieBaseFromModel(models));
        // TODO retrive a kiebase/kiesession from KnowledgeBuilderImpl
        return null;
    }

    static Model loadModel(String fullModelResourcesSourceClassName, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        try {
            String fullDrlResourcesSourceClassName = fullModelResourcesSourceClassName;
            final Class<? extends Model> aClass =
                    (Class<? extends Model>) memoryCompilerClassLoader.loadClass(fullDrlResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    static Optional<IndexFile> getIndexFile() {
        IndexFile toSearch = new IndexFile("drl");
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
