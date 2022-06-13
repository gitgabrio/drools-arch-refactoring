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
package org.kie.foo.engine.runtime.utils;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.foo.engine.api.model.FooResources;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.dar.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedExecutableResource;

public class FooRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(FooRuntimeHelper.class.getName());

    private FooRuntimeHelper() {
    }

    public static boolean canManage(FRI fri) {
        return getGeneratedExecutableResource(fri, "foo").isPresent();
    }

    @SuppressWarnings("unchecked")
    public static FooResources loadFooResources(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedExecutableResource(fri, "foo")
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        try {
            String fullBarResourcesSourceClassName = finalResource.getFullClassNames().get(0);
            final Class<? extends FooResources> aClass =
                    (Class<? extends FooResources>) memoryCompilerClassLoader.loadClass(fullBarResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    public static DAROutputFoo getDAROutput(FooResources fooResources, DARInputFoo darInputFoo) {
        return new DAROutputFoo(darInputFoo.getFRI(), darInputFoo.getInputData());
    }


}
