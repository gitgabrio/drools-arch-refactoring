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
package org.kie.drl.engine.runtime.kiesession.local.utils;

import org.drools.model.Model;
import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.common.api.model.GeneratedRedirectResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.drl.engine.runtime.kiesession.local.model.DAROutputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.model.DARInputDrlKieSessionLocal;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.kie.dar.common.api.model.FRI.SLASH;
import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;

public class DrlRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(DrlRuntimeHelper.class.getName());


    private DrlRuntimeHelper() {
    }


    public static boolean canManage(FRI fri) {
        return getGeneratedExecutableResource(fri).isPresent();
    }

    public static Optional<DAROutputDrlKieSessionLocal> execute(DARInputDrlKieSessionLocal toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        KieSession kieSession;
        try {
            kieSession = loadKieSession(toEvaluate.getFRI(), memoryCompilerClassLoader);
        } catch (Exception e) {
            logger.warn("{} can not execute {}",
                    DrlRuntimeHelper.class.getName(),
                    toEvaluate.getFRI());
            return Optional.empty();
        }
        // TODO {mfusco} :)
        if (kieSession == null) {
            return Optional.empty();
        }
        try {
            String sessionPath = toEvaluate.getFRI().getBasePath() + SLASH + kieSession.getIdentifier();
            FRI sessionFRI = new FRI(sessionPath, "drl");
            return Optional.of(new DAROutputDrlKieSessionLocal(sessionFRI, kieSession));
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s failed to execute %s",
                    DrlRuntimeHelper.class.getName(),
                    toEvaluate.getFRI()));
        }
    }


    @SuppressWarnings("unchecked")
    static KieSession loadKieSession(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedExecutableResource(fri)
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        List<Model> models = finalResource.getFullClassNames().stream().map(className -> loadModel(className, memoryCompilerClassLoader)).collect(Collectors.toList());
        // TODO {mfusco} retrieve a kiebase/kiesession from List<Model>
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
        // TODO @mfusco: define a unique subpath identifier for this specific runtime implementation, e.g.
        // The received FRI here would be the one with the specific subpath (e.g. /drl/kiesessionlocal/something) but the compiled resource
        // won't contain the "subpath", but only the main one - i.e. /drl/something)
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

}
