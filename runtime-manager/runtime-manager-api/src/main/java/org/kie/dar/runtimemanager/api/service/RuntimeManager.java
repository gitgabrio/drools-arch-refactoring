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
package org.kie.dar.runtimemanager.api.service;

import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DAROutput;

import java.util.List;
import java.util.Optional;

public interface RuntimeManager {

    Optional<DAROutput> evaluateInput(DARInput toEvaluate);

    /**
     * Produce a <code>List&lt;DAROutput&gt;</code> from the given <code>List&lt;DARInput&gt;</code>
     *
     * @param toEvaluate
     * @return
     */
     List<DAROutput> evaluateInputs(List<DARInput> toEvaluate);
}
