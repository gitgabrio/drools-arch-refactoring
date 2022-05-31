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

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.foo.engine.compilation.model.DARFinalOutputFoo;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.dar.common.utils.JavaParserUtils.getFullClassName;
import static org.kie.foo.engine.compilation.TestingUtils.*;

class FooCompilerHelperTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void getDARProcessedFoo() {
        DARResource darResourceFoo = getDARFileResource(getFileFromFileName("DarFoo.foo"));
        DARFinalOutputFoo retrieved = FooCompilerHelper.getDARProcessedFoo(darResourceFoo, memoryCompilerClassLoader);
        assertNotNull(retrieved);
        Map<String, byte[]> retrievedByteCode = retrieved.getCompiledClassesMap();
        retrievedByteCode.forEach((fullClassName, bytes) -> commonEvaluateByteCode(retrievedByteCode, fullClassName, memoryCompilerClassLoader));
    }

    @Test
    void getFooResourcesCompilationUnit() {
        Set<String> generatedSources = IntStream.range(0, 3).mapToObj(i -> "GeneratedSource" + i).collect(Collectors.toSet());
        String fooResourcesSourceClassName = "FooResourcesSourceClass";
        CompilationUnit retrieved = FooCompilerHelper.getFooResourcesCompilationUnit(generatedSources, fooResourcesSourceClassName);
        assertNotNull(retrieved);
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(getFullClassName(retrieved), retrieved.toString());
        Map<String, byte[]> compiledClasses = FooCompilerHelper.compileClasses(sourcesMap, memoryCompilerClassLoader);
        assertEquals(sourcesMap.size(), compiledClasses.size());
        commonEvaluateByteCode(compiledClasses, getFullClassName(retrieved), memoryCompilerClassLoader);
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
        Map<String, byte[]> retrieved = FooCompilerHelper.compileClasses(sourcesMap, memoryCompilerClassLoader);
        assertEquals(sourcesMap.size(), retrieved.size());
        commonEvaluateByteCode(retrieved, fullClassName, memoryCompilerClassLoader);
    }

}