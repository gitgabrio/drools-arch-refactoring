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
package org.kie.dar.compilationmanager.core.service;

import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.CompilationManager;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.kie.dar.compilationmanager.core.utils.CompilationManagerUtils.populateIndexFilesWithProcessedResource;

public class CompilationManagerImpl implements CompilationManager {
    private static final Logger logger = LoggerFactory.getLogger(CompilationManagerImpl.class.getName());

    @Override
    public List<IndexFile> processResource(DARResource toProcess, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        final List<IndexFile> toReturn = new ArrayList<>();
        populateIndexFilesWithProcessedResource(toReturn, toProcess, memoryCompilerClassLoader);
        return toReturn;
    }


//    @Override
//    public List<IndexFile>  processResources(List<DARIntermediateOutput> toProcess, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
//        return toProcess.stream()
//                .map(darResource -> this.processResource(darResource, memoryCompilerClassLoader))
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//    }


}
