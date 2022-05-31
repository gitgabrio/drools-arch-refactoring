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

import org.kie.dar.common.api.exceptions.KieDARCommonException;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARIntermediateOutput;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TestingUtils {

    private TestingUtils() {
    }

    public static void commonEvaluateByteCode(Map<String, byte[]> retrieved, String fullClassName, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        assertNotNull(retrieved);
        retrieved.forEach(memoryCompilerClassLoader::addCode);
        try {
            Class<?> loadedClass = memoryCompilerClassLoader.loadClass(fullClassName);
            loadedClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail(e);
        }
    }

    public static File getFileFromFileName(String fileName) {
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            return Paths.get(resource.toURI()).toFile();
        } catch (Exception e) {
            throw new KieDARCommonException(String.format("Failed to retrieve %s due to %s", fileName,
                    e.getMessage()), e);
        }
    }


    public static DARResource getDARResource() {
        return () -> "UnmanagedResource";
    }

    public static DARFileResource getDARFileResource(File fooFile) {
        return new DARFileResource(fooFile);
    }

    public static DARIntermediateOutput getDARResourceIntermediate() {
        return new DARIntermediateOutput(new FRI("this/is/fri","not_foo"), "foo", "Content") {
        };
    }

}
