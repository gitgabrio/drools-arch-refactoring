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
package org.kie.pmml.models.drools.commons.model;

import org.drools.drl.ast.descr.PackageDescr;
import org.kie.api.io.Resource;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.model.DARRedirectOutput;
import org.kie.dar.compilationmanager.api.model.DARSetResource;
import org.kie.dar.runtimemanager.api.model.DAROutput;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class DARRedirectOutputPMMLDrl extends DARSetResource<PackageDescr> implements DAROutput<PackageDescr> {

    private final FRI fri;
    private final PackageDescr packageDescr;

    public DARRedirectOutputPMMLDrl(FRI fri, PackageDescr packageDescr, String type) {
        super(Collections.singleton(packageDescr), type, fri.getBasePath());
        this.fri = fri;
        this.packageDescr = packageDescr;
    }

    @Override
    public FRI getFRI() {
        return fri;
    }

    @Override
    public PackageDescr getOutputData() {
        return packageDescr;
    }
}
