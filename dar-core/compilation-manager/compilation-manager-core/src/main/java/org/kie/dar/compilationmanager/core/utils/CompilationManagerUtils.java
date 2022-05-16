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

import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARFinalOutput;
import org.kie.dar.compilationmanager.api.model.DARIntermediateOutput;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.kie.dar.common.api.io.IndexFile.FINAL_SUFFIX;
import static org.kie.dar.compilationmanager.core.utils.SPIUtils.getKieCompilerService;

public class CompilationManagerUtils {

    private static final Logger logger = LoggerFactory.getLogger(CompilationManagerUtils.class.getName());

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
        // TODO
        return new IndexFile(finalOutput.getFri() + "." + finalOutput.getModelType() + FINAL_SUFFIX);
    }
}
