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

import org.kie.dar.common.api.utils.FileNameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class DARFileResource implements DARResource {

    private final File modelFile;

    public DARFileResource(File modelFile) {
        this.modelFile = modelFile;
    }

    @Override
    public Object getContent() {
        return modelFile;
    }

    public String getModelType() {
        return FileNameUtils.getSuffix(modelFile.getName());
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(modelFile);
    }

    public String getSourcePath() {
        return modelFile.getPath();
    }


}
