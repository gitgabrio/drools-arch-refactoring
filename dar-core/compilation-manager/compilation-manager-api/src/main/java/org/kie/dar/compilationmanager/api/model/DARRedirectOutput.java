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

/**
 * A generic <i>Resource</i> to be processed by specific engine
 */
public class DARRedirectOutput extends AbstractDARCallableCompilationOutput implements DARResource {

    private final String targetEngine;

    /**
     * This is the <b>payload</b> to forward to the target compilation-engine
     */
    private final Object content;

    protected DARRedirectOutput(FRI fri, String targetEngine, Object content) {
        super(fri, (List<String>) null);
        this.targetEngine = targetEngine;
        this.content = content;
    }

    public String getTargetEngine() {
        return targetEngine;
    }

    @Override
    public Object getContent() {
        return content;
    }

}
