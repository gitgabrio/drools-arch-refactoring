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

import org.drools.model.Model;
import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.utils.GeneratedResourceUtils;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.List;
import java.util.stream.Collectors;

public class DARKieSessionUtil {

    private DARKieSessionUtil() {
    }

    public static KieSession loadKieSession(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = GeneratedResourceUtils.getGeneratedExecutableResource(fri, "drl")
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
}
