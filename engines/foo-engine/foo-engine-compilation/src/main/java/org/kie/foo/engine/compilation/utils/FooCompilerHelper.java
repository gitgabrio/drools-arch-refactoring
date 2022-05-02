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
package org.kie.foo.engine.compilation.utils;

import com.github.javaparser.ast.CompilationUnit;
import org.kie.dar.common.utils.JavaParserUtils;
import org.kie.foo.engine.compilation.model.DARProcessedFoo;
import org.kie.foo.engine.compilation.model.DARResourceFoo;
import org.kie.memorycompiler.JavaConfiguration;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.util.HashMap;
import java.util.Map;

import static org.kie.dar.common.utils.JavaParserUtils.getFullClassName;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;

public class FooCompilerHelper {

    static final String FOO_MODEL_PACKAGE_NAME = "org.kie.foo.engine.compilation.model";
    static final String FOO_MODEL_TEMPLATE_JAVA = "FooModelTemplate.tmpl";
    static final String FOO_MODEL_TEMPLATE = "FooModelTemplate";

    static final KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(FooCompilerHelper.class.getClassLoader());

    private FooCompilerHelper() {
    }

    public static DARProcessedFoo getDARProcessedFoo(DARResourceFoo resource) {
        String simpleClassName = getSanitizedClassName(resource.getFullResourceName());
        CompilationUnit compilationUnit = JavaParserUtils.getCompilationUnit(simpleClassName,
                FOO_MODEL_PACKAGE_NAME,
                FOO_MODEL_TEMPLATE_JAVA,
                FOO_MODEL_TEMPLATE);
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(getFullClassName(compilationUnit), compilationUnit.toString());
        final Map<String, byte[]> compiledClasses = compileClasses(sourcesMap);
        return new DARProcessedFoo(compiledClasses);
    }

    /**
     * Compile the given sources and add them to given <code>Classloader</code> of the current instance.
     * Returns the <code>compiled bytecode</code>
     * @param sourcesMap
     * @return
     */
    static Map<String, byte[]> compileClasses(Map<String, String> sourcesMap) {
        return KieMemoryCompiler.compileNoLoad(sourcesMap, memoryClassLoader, JavaConfiguration.CompilerType.NATIVE);
    }

}
