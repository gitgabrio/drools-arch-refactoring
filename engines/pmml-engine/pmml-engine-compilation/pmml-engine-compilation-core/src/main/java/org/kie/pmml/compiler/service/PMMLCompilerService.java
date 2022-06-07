/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.pmml.compiler.service;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.utils.StringUtils;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.memorycompiler.JavaConfiguration;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.exceptions.ExternalException;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.kie.pmml.commons.model.*;
import org.kie.pmml.compiler.executor.PMMLCompiler;
import org.kie.pmml.compiler.executor.PMMLCompilerImpl;
import org.kie.pmml.compiler.impl.HasClassloaderImpl;
import org.kie.pmml.compiler.model.DARFinalOutputPMML;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.kie.dar.common.api.utils.FileNameUtils.getFileName;


/**
 * Class meant to <b>compile</b> resources
 */
public class PMMLCompilerService {

//    static final String RULES_FILE_NAME = "Rules";

    private static final PMMLCompiler PMML_COMPILER = new PMMLCompilerImpl();

    private PMMLCompilerService() {
        // Avoid instantiation
    }


    public static DARCompilationOutput getDARCompilationOutputPMML(DARFileResource resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        return getDARFinalOutputPMML(resource, memoryClassLoader);
    }

    static DARFinalOutputPMML getDARFinalOutputPMML(DARFileResource resource, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        Map<String, String> sourcesMap = new HashMap<>();
        List<KiePMMLModelWithSources> kiePmmlModels = getKiePMMLModelsFromResourcesWithConfigurationsWithSources(new HasClassloaderImpl(memoryClassLoader), Collections.singletonList(resource))
                .stream()
                .filter(KiePMMLModelWithSources.class::isInstance)
                .map(KiePMMLModelWithSources.class::cast)
                .collect(Collectors.toList());
        kiePmmlModels.forEach(kiePmmlModel -> sourcesMap.putAll(kiePmmlModel.getSourcesMap()));
        KiePMMLFactoryModel kiePMMLFactoryModel = getKiePMMLModelsFromResourcesWithConfigurationsWithSources(new HasClassloaderImpl(memoryClassLoader), Collections.singletonList(resource))
                .stream()
                .filter(KiePMMLFactoryModel.class::isInstance)
                .map(KiePMMLFactoryModel.class::cast)
                .findFirst()
                .orElseThrow(() -> new KiePMMLException("Failed to find KiePMMLModelFactory for " + resource.getSourcePath()));
        sourcesMap.putAll(kiePMMLFactoryModel.getSourcesMap());

        String fullResourceClassName = kiePMMLFactoryModel.getSourcesMap().keySet().iterator().next();

        String fileName = ((File) resource.getContent()).getName();
        String basePath = fileName.substring(0, fileName.lastIndexOf('.'));
        FRI fri = new FRI(basePath, "pmml");
        final Map<String, byte[]> compiledClasses = compileClasses(sourcesMap, memoryClassLoader);
        return new DARFinalOutputPMML(fri, fullResourceClassName, compiledClasses);
    }

    /**
     * @param hasClassLoader
     * @param resources
     * @return
     * @throws KiePMMLException  if any <code>KiePMMLInternalException</code> has been thrown during execution
     * @throws ExternalException if any other kind of <code>Exception</code> has been thrown during execution
     */
    static List<KiePMMLModel> getKiePMMLModelsFromResourcesWithConfigurationsWithSources(HasClassLoader hasClassLoader, Collection<DARFileResource> resources) {
        return resources.stream()
                .flatMap(resource -> getKiePMMLModelsFromResourceWithSources(hasClassLoader, resource).stream())
                .collect(Collectors.toList());
    }

//    /**
//     * @param hasClassLoader
//     * @param resources
//     * @return
//     * @throws KiePMMLException  if any <code>KiePMMLInternalException</code> has been thrown during execution
//     * @throws ExternalException if any other kind of <code>Exception</code> has been thrown during execution
//     */
//    static List<KiePMMLModel> getKiePMMLModelsCompiledFromResourcesWithConfigurations(HasClassLoader hasClassLoader, Collection<DARFileResource> resources) {
//        return resources.stream()
//                .flatMap(resource -> getKiePMMLModelsCompiledFromResource(hasClassLoader, resource).stream())
//                .collect(Collectors.toList());
//    }

//    /**
//     * @param hasClassLoader
//     * @param resource
//     * @return
//     * @throws KiePMMLException  if any <code>KiePMMLInternalException</code> has been thrown during execution
//     * @throws ExternalException if any other kind of <code>Exception</code> has been thrown during execution
//     */
//    static List<KiePMMLModel> getKiePMMLModelsCompiledFromResource(final HasClassLoader hasClassLoader,
//                                                                          DARFileResource resource) {
//        try {
//            String packageName = getFactoryClassNamePackageName(resource)[1];
//            return PMML_COMPILER.getKiePMMLModels(packageName, resource.getInputStream(),
//                    getFileName(resource.getSourcePath()),
//                    hasClassLoader);
//        } catch (IOException e) {
//            throw new ExternalException("ExternalException", e);
//        }
//    }

    /**
     * @param hasClassLoader
     * @param resource
     * @return
     */
    static List<KiePMMLModel> getKiePMMLModelsFromResourceWithSources(HasClassLoader hasClassLoader,
                                                                             DARFileResource resource) {
        String[] classNamePackageName = getFactoryClassNamePackageName(resource);
        String factoryClassName = classNamePackageName[0];
        String packageName = classNamePackageName[1];
        try {
            final List<KiePMMLModel> toReturn = PMML_COMPILER.getKiePMMLModelsWithSources(factoryClassName, packageName,
                    resource.getInputStream(),
                    getFileName(resource.getSourcePath()),
                    hasClassLoader);
            return toReturn;
        } catch (IOException e) {
            throw new ExternalException("ExternalException", e);
        }
    }

    /**
     * Returns an array where the first item is the <b>factory class</b> name and the second item is the <b>package</b> name,
     * built starting from the given <code>Resource</code>
     *
     * @param resource
     * @return
     */
    static String[] getFactoryClassNamePackageName(DARFileResource resource) {
        String sourcePath = resource.getSourcePath();
        if (sourcePath == null || sourcePath.isEmpty()) {
            throw new IllegalArgumentException("Missing required sourcePath in resource " + resource + " -> " + resource.getClass().getName());
        }
        return StringUtils.getFactoryClassNamePackageName(sourcePath);
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
