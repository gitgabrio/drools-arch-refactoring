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

import java.util.Collections;
import java.util.List;

public abstract class AbstractDARCallableCompilationOutput implements DARCallableOutput {

    private final FRI fri;
    private final List<String> fullClassNames;

    protected AbstractDARCallableCompilationOutput(FRI fri, String fullClassName) {
        this(fri, Collections.singletonList(fullClassName));
    }

    protected AbstractDARCallableCompilationOutput(FRI fri, List<String> fullClassNames) {
        this.fri = fri;
        this.fullClassNames = fullClassNames;
    }


    /**
     * Returns the <b>full resource identifier</b> to be invoked for execution
     *
     * @return
     */
    @Override
    public FRI getFri() {
        return fri;
    }

    @Override
    public List<String> getFullClassNames() {
        return fullClassNames;
    }

}
