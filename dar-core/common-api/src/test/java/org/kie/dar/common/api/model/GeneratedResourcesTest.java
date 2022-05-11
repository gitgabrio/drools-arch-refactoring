package org.kie.dar.common.api.model;/*
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedResourcesTest {

    @Test
    void add() {
        String fullPath = "full/path";
        String type = "type";
        GeneratedResource generatedIntermediateResource = new GeneratedIntermediateResource(fullPath, type);
        String frn = "this/is/frn";
        GeneratedResource generatedFinalResource = new GeneratedFinalResource(fullPath, type, frn);
        GeneratedResources generatedResources = new GeneratedResources();
        generatedResources.add(generatedIntermediateResource);
        generatedResources.add(generatedFinalResource);
        assertEquals(1, generatedResources.size());

        generatedResources = new GeneratedResources();
        generatedResources.add(new GeneratedFinalResource("fullPath/a", type, frn));
        generatedResources.add(new GeneratedFinalResource("fullPath/b", type, frn));
        assertEquals(1, generatedResources.size());


        String fullPathIntermediate = "full/path/intermediate";
        generatedIntermediateResource = new GeneratedIntermediateResource(fullPathIntermediate, type);
        String fullPathFinal = "full/path/final";
        generatedFinalResource = new GeneratedFinalResource(fullPathFinal, type, frn);
        generatedResources = new GeneratedResources();
        generatedResources.add(generatedIntermediateResource);
        generatedResources.add(generatedFinalResource);
        assertEquals(2, generatedResources.size());
        assertTrue(generatedResources.contains(generatedIntermediateResource));
        assertTrue(generatedResources.contains(generatedFinalResource));
    }

}