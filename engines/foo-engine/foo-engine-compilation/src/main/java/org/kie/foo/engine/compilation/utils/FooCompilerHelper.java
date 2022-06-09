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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.utils.JavaParserUtils;
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARRedirectOutput;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.foo.engine.compilation.model.DARCallableOutputFoo;
import org.kie.memorycompiler.JavaConfiguration;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.kie.dar.common.utils.CommonCodegenUtils.getSuperConstructorInvocation;
import static org.kie.dar.common.utils.JavaParserUtils.getFullClassName;
import static org.kie.dar.common.utils.StringUtils.getSanitizedClassName;
import static org.kie.foo.engine.api.constants.Constants.FOO_MODEL_PACKAGE_NAME;

public class FooCompilerHelper {
    static final String FOO_MODEL_TEMPLATE_JAVA = "FooModelTemplate.tmpl";
    static final String FOO_MODEL_TEMPLATE = "FooModelTemplate";

    static final String FOO_RESOURCES_TEMPLATE_JAVA = "FooResourcesTemplate.tmpl";

    static final String FOO_RESOURCES_TEMPLATE = "FooResourcesTemplate";

    private FooCompilerHelper() {
    }

    public static DARCallableOutputFoo getDARProcessedFoo(DARResource resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        if (resource instanceof DARFileResource) {
            return getDARProcessedFooFromFile((DARFileResource) resource, memoryClassLoader);
        } else if (resource instanceof DARRedirectOutput) {
            return getDARProcessedFooFromIntermediate((DARRedirectOutput) resource, memoryClassLoader);
        } else {
            throw new KieCompilerServiceException("Unexpected DARIntermediateOutputFoo " + resource.getClass());
        }
    }

    static DARCallableOutputFoo getDARProcessedFooFromFile(DARFileResource resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        FRI fri = new FRI(((File) resource.getContent()).getAbsolutePath(), "foo");
        String simpleClassName = getSanitizedClassName(fri.getFri());
        CompilationUnit compilationUnit = JavaParserUtils.getCompilationUnit(simpleClassName,
                FOO_MODEL_PACKAGE_NAME,
                FOO_MODEL_TEMPLATE_JAVA,
                FOO_MODEL_TEMPLATE);
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(getFullClassName(compilationUnit), compilationUnit.toString());
        String fooResourcesSourceClassName = getSanitizedClassName(simpleClassName + "Resources");
        CompilationUnit fooResourcesSourceCompilationUnit = getFooResourcesCompilationUnit(sourcesMap.keySet(), fooResourcesSourceClassName);
        String fullResourcesClassName = getFullClassName(fooResourcesSourceCompilationUnit);
        sourcesMap.put(fullResourcesClassName, fooResourcesSourceCompilationUnit.toString());
        final Map<String, byte[]> compiledClasses = compileClasses(sourcesMap, memoryClassLoader);
        return new DARCallableOutputFoo(fri, fullResourcesClassName, compiledClasses);
    }

    static DARCallableOutputFoo getDARProcessedFooFromIntermediate(DARRedirectOutput resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        FRI fooFri = new FRI(resource.getFri().getBasePath(), "foo");
        String simpleClassName = getSanitizedClassName(fooFri.getFri());
        CompilationUnit compilationUnit = JavaParserUtils.getCompilationUnit(simpleClassName,
                FOO_MODEL_PACKAGE_NAME,
                FOO_MODEL_TEMPLATE_JAVA,
                FOO_MODEL_TEMPLATE);
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(getFullClassName(compilationUnit), compilationUnit.toString());
        String fooResourcesSourceClassName = getSanitizedClassName(simpleClassName + "Resources");
        CompilationUnit fooResourcesSourceCompilationUnit = getFooResourcesCompilationUnit(sourcesMap.keySet(), fooResourcesSourceClassName);
        String fullResourcesClassName = getFullClassName(fooResourcesSourceCompilationUnit);
        sourcesMap.put(fullResourcesClassName, fooResourcesSourceCompilationUnit.toString());
        final Map<String, byte[]> compiledClasses = compileClasses(sourcesMap, memoryClassLoader);
        return new DARCallableOutputFoo(fooFri, fullResourcesClassName, compiledClasses);
    }

    static CompilationUnit getFooResourcesCompilationUnit(Set<String> generatedSources, String fooResourcesSourceClassName) {
        CompilationUnit toReturn = JavaParserUtils.getCompilationUnit(fooResourcesSourceClassName,
                FOO_MODEL_PACKAGE_NAME,
                FOO_RESOURCES_TEMPLATE_JAVA,
                FOO_RESOURCES_TEMPLATE);
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = toReturn.getClassByName(fooResourcesSourceClassName)
                .orElseThrow(() -> new KieCompilerServiceException("Failed to find expected class " + fooResourcesSourceClassName));
        ConstructorDeclaration constructorDeclaration = classOrInterfaceDeclaration.getConstructors().get(0);
        constructorDeclaration.setName(new SimpleName(fooResourcesSourceClassName));
        final NodeList<Expression> generatedSourcesArgument = new NodeList<>();
        for (String generatedSource : generatedSources) {
            generatedSourcesArgument.add(new StringLiteralExpr(generatedSource));
        }
        ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt = getSuperConstructorInvocation(constructorDeclaration);
        explicitConstructorInvocationStmt.getArguments().get(0).asMethodCallExpr().setArguments(generatedSourcesArgument);
        return toReturn;
    }

    /**
     * Compile the given sources and add them to given <code>Classloader</code> of the current instance.
     * Returns the <code>compiled bytecode</code>
     *
     * @param sourcesMap
     * @return
     */
    static Map<String, byte[]> compileClasses(Map<String, String> sourcesMap, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        return KieMemoryCompiler.compileNoLoad(sourcesMap, memoryClassLoader, JavaConfiguration.CompilerType.NATIVE);
    }

}
