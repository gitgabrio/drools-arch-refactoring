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
import org.kie.dar.common.api.model.*;
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.*;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.memorycompiler.KieMemoryCompilerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.kie.dar.common.api.constants.Constants.INDEXFILE_DIRECTORY_PROPERTY;
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
            toPopulate.add(getIndexFileFromCompilationOutput(darCompilationOutput));
            if (darCompilationOutput instanceof DARFinalOutputClassesContainer) {
                loadClasses(((DARFinalOutputClassesContainer) darCompilationOutput).getCompiledClassesMap(), memoryCompilerClassLoader);
            }
            if (darCompilationOutput instanceof DARIntermediateOutput) {
                populateIndexFilesWithProcessedResource(toPopulate, (DARIntermediateOutput) darCompilationOutput, memoryCompilerClassLoader);
            }
        });
    }

    static IndexFile getIndexFileFromCompilationOutput(DARCompilationOutput compilationOutput) {
        IndexFile toReturn = getIndexFile(compilationOutput);
        populateIndexFile(toReturn, compilationOutput);
        return toReturn;
    }

    static IndexFile getIndexFile(DARCompilationOutput compilationOutput) {
        String parentPath = System.getProperty(INDEXFILE_DIRECTORY_PROPERTY, DEFAULT_INDEXFILE_DIRECTORY);
        IndexFile toReturn = new IndexFile(parentPath, compilationOutput.getFri().getModel());
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

    static void populateIndexFile(IndexFile toPopulate, DARCompilationOutput compilationOutput) {
        try {
            GeneratedResources generatedResources = getGeneratedResourcesObject(toPopulate);
            populateGeneratedResources(generatedResources, compilationOutput);
            writeGeneratedResourcesObject(generatedResources, toPopulate);
        } catch (IOException e) {
            throw new KieCompilerServiceException(e);
        }
    }

    static void populateGeneratedResources(GeneratedResources toPopulate, DARCompilationOutput compilationOutput) {
        toPopulate.add(getGeneratedResource(compilationOutput));
        if (compilationOutput instanceof DARClassesContainer) {
            toPopulate.addAll(getGeneratedResources((DARClassesContainer) compilationOutput));
        }
    }

    static GeneratedResource getGeneratedResource(DARCompilationOutput compilationOutput) {
        if (compilationOutput instanceof DARFinalOutput) {
            return new GeneratedExecutableResource(compilationOutput.getFri(), ((DARFinalOutput)compilationOutput).getFullClassName());
        } else if (compilationOutput instanceof DARIntermediateOutput) {
            return new GeneratedRedirectResource(compilationOutput.getFri(), ((DARIntermediateOutput) compilationOutput).getTargetEngine());
        } else {
            throw new KieCompilerServiceException("Unmanaged type " + compilationOutput.getClass().getName());
        }

    }

    static List<GeneratedResource> getGeneratedResources(DARClassesContainer finalOutput) {
        return finalOutput.getCompiledClassesMap().keySet().stream()
                .map(CompilationManagerUtils::getGeneratedClassResource)
                .collect(Collectors.toList());
    }

    static GeneratedClassResource getGeneratedClassResource(String fullClassName) {
        return new GeneratedClassResource(fullClassName);
    }

    static void loadClasses(Map<String, byte[]> compiledClassesMap, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        for (Map.Entry<String, byte[]> entry : compiledClassesMap.entrySet()) {
            memoryCompilerClassLoader.addCode(entry.getKey(), entry.getValue());
            try {
                memoryCompilerClassLoader.loadClass(entry.getKey());
            } catch (ClassNotFoundException e) {
                throw new KieMemoryCompilerException(e.getMessage(), e);
            }
        }
    }
}


