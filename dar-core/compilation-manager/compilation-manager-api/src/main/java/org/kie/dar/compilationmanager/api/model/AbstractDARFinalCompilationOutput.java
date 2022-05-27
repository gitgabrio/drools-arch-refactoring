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

public abstract class AbstractDARFinalCompilationOutput extends AbstractDARCompilationOutput implements DARFinalOutput {

    private final String fri;
    private final String fullClassName;

    protected AbstractDARFinalCompilationOutput(String fri, String modelType, String fullClassName) {
        super(modelType);
        this.fri = fri;
        this.fullClassName = fullClassName;
    }

    @Override
    public String getFri() {
        return fri;
    }

    @Override
    public String getFullClassName() {
        return fullClassName;
    }
}
