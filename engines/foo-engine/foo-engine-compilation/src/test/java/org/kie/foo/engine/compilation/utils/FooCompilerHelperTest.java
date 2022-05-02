package org.kie.foo.engine.compilation.utils;/*
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
import org.kie.foo.engine.compilation.model.DARProcessedFoo;
import org.kie.foo.engine.compilation.model.DARResourceFoo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.foo.engine.compilation.utils.FooCompilerHelper.FOO_MODEL_PACKAGE_NAME;
import static org.kie.foo.engine.compilation.utils.FooCompilerHelper.memoryClassLoader;

class FooCompilerHelperTest {

    @Test
    void getDARProcessedFoo() {
        DARResourceFoo darResourceFoo = new DARResourceFoo("fullResourceName");
        DARProcessedFoo retrieved = FooCompilerHelper.getDARProcessedFoo(darResourceFoo);
        assertNotNull(retrieved);
        Map<String, byte[]> retrievedByteCode = retrieved.getCompiledClassesMap();
        String fullClassName = FOO_MODEL_PACKAGE_NAME + "." + getSanitizedClassName(darResourceFoo.getFullResourceName());
        commonEvaluateByteCode(retrievedByteCode, fullClassName);
    }

    @Test
    void compileClasses() {
        String fullClassName = "org.kie.foo.engine.compilation.model.TestingSource";
        String testingSource = "package org.kie.foo.engine.compilation.model;\n" +
                "\n" +
                "public class TestingSource {\n" +
                "}";
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(fullClassName, testingSource);
        Map<String, byte[]> retrieved = FooCompilerHelper.compileClasses(sourcesMap);
        assertEquals(sourcesMap.size(), retrieved.size());
        commonEvaluateByteCode(retrieved, fullClassName);
    }

    private void commonEvaluateByteCode(Map<String, byte[]> retrieved, String fullClassName) {
        assertNotNull(retrieved);
        retrieved.forEach(memoryClassLoader::addCode);
        try {
            Class<?> loadedClass = memoryClassLoader.loadClass(fullClassName);
            loadedClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail(e);
        }
    }
}