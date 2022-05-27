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

import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.io.IndexFile;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.common.api.model.GeneratedResources;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.foo.engine.api.model.FooResources;
import org.kie.foo.engine.runtime.model.DARInputFoo;
import org.kie.foo.engine.runtime.model.DAROutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;
import static org.kie.dar.common.api.utils.JSONUtils.getGeneratedResourcesObject;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.foo.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;

public class FooRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(FooRuntimeHelper.class.getName());

    private FooRuntimeHelper() {
    }

    public static boolean canManage(FRI fri) {
        return getGeneratedFinalResource(fri).isPresent();
    }

    public static FooResources loadFooResources(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedFinalResource(fri)
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        String fullBarResourcesSourceClassName = finalResource.getFullClassName();
        try {
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

    static Optional<GeneratedExecutableResource> getGeneratedFinalResource(FRI fri) {
        IndexFile toSearch = new IndexFile("foo");
        File existingFile;
        try {
            existingFile = getFileFromFileName(toSearch.getName());
            toSearch = new IndexFile(existingFile);
            logger.debug("IndexFile {} exists", toSearch.getName());
        } catch (KieDARCommonException e) {
            logger.debug("IndexFile {} does not exists.", toSearch.getName());
            return Optional.empty();
        }
        try {
            GeneratedResources generatedResources = getGeneratedResourcesObject(toSearch);
            return generatedResources.stream()
                    .filter(generatedResource -> generatedResource instanceof GeneratedExecutableResource &&
                            ((GeneratedExecutableResource)generatedResource).getFri().equals(fri))
                    .findFirst()
                    .map(GeneratedExecutableResource.class::cast);
        } catch (IOException e) {
            logger.debug("Failed to read GeneratedResources from {}.", toSearch.getName(), e);
            return Optional.empty();
        }
    }
}
