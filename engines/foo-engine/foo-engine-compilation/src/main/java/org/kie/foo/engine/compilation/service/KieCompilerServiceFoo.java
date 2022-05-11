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
package org.kie.foo.engine.compilation.service;

import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.model.DARResourceFileContainer;
import org.kie.dar.compilationmanager.api.model.DARResourceIntermediate;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.foo.engine.compilation.model.DARResourceFileFoo;
import org.kie.foo.engine.compilation.model.DARResourceFoo;
import org.kie.foo.engine.compilation.model.DARResourceIntermediateFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.kie.foo.engine.compilation.utils.FooCompilerHelper.getDARProcessedFoo;

public class KieCompilerServiceFoo implements KieCompilerService {

    @Override
    public <T extends DARResource> boolean canManageResource(T toProcess) {
        if (toProcess instanceof DARResourceFileContainer && ((DARResourceFileContainer) toProcess).getModelFile().getName().endsWith(".foo")) {
            return true;
        } else if (toProcess instanceof DARResourceIntermediate && ((DARResourceIntermediate) toProcess).getTargetEngine().equalsIgnoreCase("foo")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DARResource, E extends DARCompilationOutput> E processResource(T toProcess, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilerServiceException(String.format("%s can not process %s",
                    this.getClass().getName(),
                    toProcess.getClass().getName()));
        }
        DARResourceFoo toManage = toProcess instanceof DARResourceFileContainer ? new DARResourceFileFoo((DARResourceFileContainer) toProcess) :
                new DARResourceIntermediateFoo((DARResourceIntermediate) toProcess);
        return (E) getDARProcessedFoo(toManage, memoryCompilerClassLoader);
    }
}
