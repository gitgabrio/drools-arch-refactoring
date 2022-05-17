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
package org.kie.dar.compilationmanager.core.utils;

import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.GeneratedFinalResource;
import org.kie.dar.common.api.model.GeneratedIntermediateResource;
import org.kie.dar.common.api.model.GeneratedResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.common.api.utils.FileUtils;
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.*;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.kie.dar.common.api.constants.Constants.INDEXFILE_DIRECTORY_PROPERTY;
import static org.kie.dar.common.api.io.IndexFile.FINAL_SUFFIX;
import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;
import static org.kie.dar.common.api.utils.JSONUtils.writeGeneratedResourcesObject;
import static org.kie.dar.compilationmanager.core.utils.SPIUtils.getKieCompilerService;

public class CompilationManagerUtils {

    private static final Logger logger = LoggerFactory.getLogger(CompilationManagerUtils.class.getName());
    private static final String DEFAULT_INDEXFILE_DIRECTORY = "./target/classes";

    private CompilationManagerUtils() {
    }

    public static void populateIndexFilesWithProcessedResource(final List<IndexFile> toPopulate, DARResource toProcess, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        Optional<KieCompilerService> retrieved = getKieCompilerService(toProcess, true);
        if (retrieved.isEmpty()) {
            logger.warn("Cannot find KieCompilerService for {}", toProcess.getClass());
        }
        Optional<DARCompilationOutput> darCompilationOutputOptional = retrieved.map(service -> service.processResource(toProcess, memoryCompilerClassLoader));
        darCompilationOutputOptional.ifPresent(darCompilationOutput -> {
            if (darCompilationOutput instanceof DARFinalOutput) {
                toPopulate.add(getIndexFileFromFinalOutput((DARFinalOutput) darCompilationOutput));
            } else {
                populateIndexFilesWithProcessedResource(toPopulate, (DARIntermediateOutput) darCompilationOutput, memoryCompilerClassLoader);
            }
        });
    }

    static IndexFile getIndexFileFromFinalOutput(DARFinalOutput finalOutput) {
        IndexFile toReturn = getIndexFile(finalOutput);
        populateIndexFile(toReturn, finalOutput);
        return  toReturn;
    }

    static IndexFile getIndexFile(DARFinalOutput finalOutput) {
        String parentPath = System.getProperty(INDEXFILE_DIRECTORY_PROPERTY, DEFAULT_INDEXFILE_DIRECTORY);
        IndexFile toReturn = new IndexFile(parentPath, finalOutput.getModelType());
        File existingFile;
        try {
            existingFile = getFileFromFileName(toReturn.getName());
            toReturn = new IndexFile(existingFile);
            logger.debug("IndexFile {} already exists", toReturn.getName());
        } catch (KieDARCommonException e) {
            logger.debug("IndexFile {} does not exists, creating it...", toReturn.getName());
            createIndexFile(toReturn);
        }
        return toReturn;
    }

    static void createIndexFile(IndexFile toCreate) {
        try {
            logger.debug("Writing file {}", toCreate.getPath());
            if (!toCreate.createNewFile()) {
                throw new KieCompilerServiceException("Failed to create " + toCreate.getName());
            }
        } catch (IOException e) {
            logger.error("Failed to create {} due to {}", toCreate.getName(), e);
           throw new KieCompilerServiceException("Failed to create " + toCreate.getName(), e);
        }
    }

    static void populateIndexFile(IndexFile toPopulate, DARFinalOutput finalOutput) {
        try {
            GeneratedResources generatedResources = getGeneratedResourcesObject(toPopulate);
            populateGeneratedResources(generatedResources, finalOutput);
            writeGeneratedResourcesObject(generatedResources, toPopulate);
        } catch (IOException e) {
            throw new KieCompilerServiceException(e);
        }
    }

    static void populateGeneratedResources(GeneratedResources toPopulate, DARFinalOutput finalOutput) {
        toPopulate.add(getGeneratedResource(finalOutput));
        if (finalOutput instanceof DARFinalOutputClassesContainer) {
            toPopulate.addAll(getGeneratedResources((DARFinalOutputClassesContainer) finalOutput));
        }
    }

    static GeneratedResource getGeneratedResource(DARFinalOutput finalOutput) {
        return new GeneratedFinalResource(finalOutput.toString(), finalOutput.getModelType(), finalOutput.getFri());
    }

    static List<GeneratedResource> getGeneratedResources(DARFinalOutputClassesContainer finalOutput) {
        return finalOutput.getCompiledClassesMap().keySet().stream().map(CompilationManagerUtils::GeneratedIntermediateResource).collect(Collectors.toList());
    }

    static GeneratedResource GeneratedIntermediateResource(String className) {
        return new GeneratedIntermediateResource(className, "class");
    }


}
