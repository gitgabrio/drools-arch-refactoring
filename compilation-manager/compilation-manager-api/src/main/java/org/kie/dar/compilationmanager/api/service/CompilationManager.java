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
package org.kie.dar.compilationmanager.api.service;

import org.kie.dar.compilationmanager.api.model.DARProcessed;
import org.kie.dar.compilationmanager.api.model.DARResource;

import java.util.List;
import java.util.Optional;

public interface CompilationManager {

    /**
     * Produce one <code>DARProcessed</code> from the given <code>DARResource</code>.
     * The return is <code>Optional</code> because the engine required to process given <code>DARResource</code>
     * may not be found
     * @param toProcess
     * @return
     */
    Optional<DARProcessed> processResource(DARResource toProcess);

    /**
     * Produce a <code>List&lt;DARProcessed&gt;</code> from the given <code>List&lt;DARResource&gt;</code>
     * @param toProcess
     * @return
     */
    List<DARProcessed> processResources(List<DARResource> toProcess);


}
