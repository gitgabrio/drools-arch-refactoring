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
package org.kie.dar.compilationmanager.api.model;

import org.kie.dar.common.api.model.FRI;

import java.util.List;
import java.util.Map;

/**
 * A <code>DARCallableOutput</code> containing compiled classes
 */
public abstract class DARCallableOutputClassesContainer extends AbstractDARCallableCompilationOutput implements DARClassesContainer {

    private final Map<String, byte[]> compiledClassMap;

    protected DARCallableOutputClassesContainer(FRI fri, String fullClassName, Map<String, byte[]> compiledClassMap) {
        super(fri, fullClassName);
        this.compiledClassMap = compiledClassMap;
    }

    protected DARCallableOutputClassesContainer(FRI fri, List<String> fullClassNames, Map<String, byte[]> compiledClassMap) {
        super(fri, fullClassNames);
        this.compiledClassMap = compiledClassMap;
    }

    @Override
    public Map<String, byte[]> getCompiledClassesMap() {
        return compiledClassMap;
    }
}
