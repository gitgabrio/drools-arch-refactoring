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
package org.kie.dar.runtimemanager.api.mocks;


import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.model.AbstractDAROutput;

public class MockDAROutput extends AbstractDAROutput {

    public MockDAROutput() {
        super(new FRI(MockDAROutput.class.getCanonicalName().replace('.', '/'), "mock"));
    }

    @Override
    public Object getOutputData() {
        return null;
    }
}