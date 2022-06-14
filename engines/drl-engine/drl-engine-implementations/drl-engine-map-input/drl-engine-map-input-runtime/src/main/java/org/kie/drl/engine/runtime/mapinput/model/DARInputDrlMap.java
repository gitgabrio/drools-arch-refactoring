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
package org.kie.drl.engine.runtime.mapinput.model;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.model.DARMapInputDTO;
import org.kie.drl.engine.runtime.model.DARInputDrl;

/**
 * <code>DARInputDrl</code> specific for map input usage.
 * Its scope it is to provide a <code>Map</code> with the data needed for the evaluation.
 * To be used, for example, by PMML
 */
public class DARInputDrlMap extends DARInputDrl<DARMapInputDTO> {

    public DARInputDrlMap(FRI fri, DARMapInputDTO inputData) {
        super(fri, inputData);
    }
}
