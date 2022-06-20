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

import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoRedirectOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TestingUtils {

    private TestingUtils() {
    }

    public static void commonEvaluateByteCode(Map<String, byte[]> retrieved, String fullClassName, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        assertThat(retrieved).isNotNull();
        retrieved.forEach(memoryCompilerClassLoader::addCode);
        try {
            Class<?> loadedClass = memoryCompilerClassLoader.loadClass(fullClassName);
            loadedClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail("", e);
        }
    }

    public static File getFileFromFileName(String fileName) {
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
            return Paths.get(resource.toURI()).toFile();
        } catch (Exception e) {
            throw new KieEfestoCommonException(String.format("Failed to retrieve %s due to %s", fileName,
                    e.getMessage()), e);
        }
    }


    public static EfestoResource<String> getEfestoResource() {
        return () -> "UnmanagedResource";
    }

    public static EfestoFileResource getEfestoFileResource(File fooFile) {
        return new EfestoFileResource(fooFile);
    }

    public static EfestoRedirectOutput<String> getEfestoResourceIntermediate() {
        return new EfestoRedirectOutput(new FRI("this/is/fri", "not_foo"), "foo", "Content") {
        };
    }

}
