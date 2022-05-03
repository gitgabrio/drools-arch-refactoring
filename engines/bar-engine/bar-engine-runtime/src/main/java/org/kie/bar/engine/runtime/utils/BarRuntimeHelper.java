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

import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.bar.engine.api.model.BarResources;
import org.kie.bar.engine.runtime.model.DARInputBar;
import org.kie.bar.engine.runtime.model.DAROutputBar;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.bar.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;

public class BarRuntimeHelper {

    private BarRuntimeHelper() {
    }


    public static BarResources loadBarResources(String fullResourceName, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader)   {
        String simpleClassName = getSanitizedClassName(fullResourceName) + "Resources";
        String fullBarResourcesSourceClassName = FOO_MODEL_PACKAGE_NAME + "." + simpleClassName;
        try {
            final Class<? extends BarResources> aClass =
                    (Class<? extends BarResources>) memoryCompilerClassLoader.loadClass(fullBarResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    public static DAROutputBar getDAROutput(BarResources fooResources, DARInputBar darInputBar) {
        return new DAROutputBar(darInputBar.getFullResourceName(), darInputBar.getInputData());
    }
}
