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
package org.kie.foo.engine.compilation;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.foo.engine.compilation.utils.FooCompilerHelper.memoryClassLoader;

public class TestingUtils {

    private TestingUtils() {
    }
    public static void commonEvaluateByteCode(Map<String, byte[]> retrieved, String fullClassName) {
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
