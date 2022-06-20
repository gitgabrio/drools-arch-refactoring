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
package org.kie.bar.engine.compilation.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import org.kie.bar.engine.compilation.model.EfestoCallableOutputBar;
import org.kie.bar.engine.compilation.model.EfestoRedirectOutputBar;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.common.utils.JavaParserUtils;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.memorycompiler.JavaConfiguration;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.kie.bar.engine.api.constants.Constants.BAR_MODEL_PACKAGE_NAME;
import static org.kie.efesto.common.utils.CommonCodegenUtils.getSuperConstructorInvocation;
import static org.kie.efesto.common.utils.JavaParserUtils.getFullClassName;
import static org.kie.efesto.common.utils.StringUtils.getSanitizedClassName;

public class BarCompilerHelper {

    static final String BAR_MODEL_TEMPLATE_JAVA = "BarModelTemplate.tmpl";
    static final String BAR_MODEL_TEMPLATE = "BarModelTemplate";

    static final String BAR_RESOURCES_TEMPLATE_JAVA = "BarResourcesTemplate.tmpl";

    static final String BAR_RESOURCES_TEMPLATE = "BarResourcesTemplate";

    private BarCompilerHelper() {
    }

    public static EfestoCompilationOutput getEfestoCompilationOutputBar(EfestoFileResource resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        if ((resource.getContent()).getName().startsWith("Redirect")) {
            return getEfestoRedirectOutputBar(resource);
        } else {
            return getEfestoFinalOutputBar(resource, memoryClassLoader);
        }
    }

    static EfestoCallableOutputBar getEfestoFinalOutputBar(EfestoFileResource resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        String fileName = ( resource.getContent()).getName().toLowerCase();
        String basePath = fileName.substring(0, fileName.lastIndexOf('.'));
        FRI fri = new FRI(basePath, "bar");
        String simpleClassName = getSanitizedClassName(fri.getFri());
        CompilationUnit compilationUnit = JavaParserUtils.getCompilationUnit(simpleClassName,
                BAR_MODEL_PACKAGE_NAME,
                BAR_MODEL_TEMPLATE_JAVA,
                BAR_MODEL_TEMPLATE);
        Map<String, String> sourcesMap = new HashMap<>();
        sourcesMap.put(getFullClassName(compilationUnit), compilationUnit.toString());
        String barResourcesSourceClassName = getSanitizedClassName(simpleClassName + "Resources");
        CompilationUnit barResourcesSourceCompilationUnit = getBarResourcesCompilationUnit(sourcesMap.keySet(), barResourcesSourceClassName);
        String fullResourceClassName = getFullClassName(barResourcesSourceCompilationUnit);
        sourcesMap.put(fullResourceClassName, barResourcesSourceCompilationUnit.toString());
        final Map<String, byte[]> compiledClasses = compileClasses(sourcesMap, memoryClassLoader);
        return new EfestoCallableOutputBar(fri, fullResourceClassName, compiledClasses);
    }

    static EfestoRedirectOutputBar getEfestoRedirectOutputBar(EfestoFileResource resource) {
        String fileName = (resource.getContent()).getName().toLowerCase();
        String basePath = fileName.substring(0, fileName.lastIndexOf('.'));
        FRI fri = new FRI(basePath, "bar");
        return new EfestoRedirectOutputBar(fri, resource.getContent());
    }

    static CompilationUnit getBarResourcesCompilationUnit(Set<String> generatedSources, String barResourcesSourceClassName) {
        CompilationUnit toReturn = JavaParserUtils.getCompilationUnit(barResourcesSourceClassName,
                BAR_MODEL_PACKAGE_NAME,
                BAR_RESOURCES_TEMPLATE_JAVA,
                BAR_RESOURCES_TEMPLATE);
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = toReturn.getClassByName(barResourcesSourceClassName)
                .orElseThrow(() -> new KieCompilerServiceException("Failed to find expected class " + barResourcesSourceClassName));
        ConstructorDeclaration constructorDeclaration = classOrInterfaceDeclaration.getConstructors().get(0);
        constructorDeclaration.setName(new SimpleName(barResourcesSourceClassName));
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
