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
package org.kie.dar.common.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.dar.common.api.model.GeneratedResource;
import org.kie.dar.common.api.model.GeneratedResources;

public class JSONUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JSONUtils() {
    }

    public static String getGeneratedResourceString(GeneratedResource generatedResource) throws JsonProcessingException {
        return objectMapper.writeValueAsString(generatedResource);
    }

    public static GeneratedResource getGeneratedResourceObject(String generatedResourceString) throws JsonProcessingException {
        return objectMapper.readValue(generatedResourceString, GeneratedResource.class);
    }

    public static String getGeneratedResourcesString(GeneratedResources generatedResources) throws JsonProcessingException {
        return objectMapper.writeValueAsString(generatedResources);
    }

    public static GeneratedResources getGeneratedResourcesObject(String generatedResourcesString) throws JsonProcessingException {
        return objectMapper.readValue(generatedResourcesString, GeneratedResources.class);
    }
}
