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

import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.foo.engine.api.model.FooResources;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;

import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.foo.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;

public class FooRuntimeHelper {

    private FooRuntimeHelper() {
    }


    public static FooResources loadFooResources(String fullResourceName)   {
        String simpleClassName = getSanitizedClassName(fullResourceName) + "Resources";
        String fullFooResourcesSourceClassName = FOO_MODEL_PACKAGE_NAME + "." + simpleClassName;
        ClassLoader classLoader = FooRuntimeHelper.class.getClassLoader();
        try {
            final Class<? extends FooResources> aClass =
                    (Class<? extends FooResources>) classLoader.loadClass(fullFooResourcesSourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    public static DAROutputFoo getDAROutput(FooResources fooResources, DARInputFoo darInputFoo) {
        return new DAROutputFoo(darInputFoo.getFullResourceName(), darInputFoo.getInputData());
    }
}
