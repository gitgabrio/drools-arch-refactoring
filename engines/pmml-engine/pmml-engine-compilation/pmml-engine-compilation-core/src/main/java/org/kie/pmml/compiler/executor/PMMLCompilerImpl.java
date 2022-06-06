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
package org.kie.pmml.compiler.executor;

import org.dmg.pmml.PMML;
import org.kie.pmml.api.exceptions.ExternalException;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.kie.pmml.api.exceptions.KiePMMLInternalException;
import org.kie.pmml.commons.model.HasClassLoader;
import org.kie.pmml.commons.model.HasSourcesMap;
import org.kie.pmml.commons.model.KiePMMLFactoryModel;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.compiler.api.dto.CommonCompilationDTO;
import org.kie.pmml.compiler.commons.utils.KiePMMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.kie.pmml.commons.Constants.PACKAGE_CLASS_TEMPLATE;
import static org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedClassName;
import static org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedPackageName;
import static org.kie.pmml.compiler.commons.factories.KiePMMLFactoryFactory.getFactorySourceCode;
import static org.kie.pmml.compiler.commons.implementations.KiePMMLModelRetriever.*;

/**
 * <code>PMMLCompiler</code> default implementation
 */
public class PMMLCompilerImpl implements PMMLCompiler {

    private static final Logger logger = LoggerFactory.getLogger(PMMLCompilerImpl.class.getName());

    @Override
    public List<KiePMMLModel> getKiePMMLModels(final String packageName, final InputStream inputStream,
                                               final String fileName, final HasClassLoader hasClassloader) {
        logger.trace("getModels {} {} {}", packageName, inputStream, hasClassloader);
        try {
            PMML commonPMMLModel = KiePMMLUtil.load(inputStream, fileName);
            return getModels(packageName, commonPMMLModel, hasClassloader);
        } catch (KiePMMLInternalException e) {
            throw new KiePMMLException("KiePMMLInternalException", e);
        } catch (KiePMMLException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalException("ExternalException", e);
        }
    }

    @Override
    public List<KiePMMLModel> getKiePMMLModelsWithSources(final String factoryClassName,
                                                          final String packageName,
                                                          final InputStream inputStream,
                                                          final String fileName,
                                                          final HasClassLoader hasClassLoader) {
        logger.trace("getModels {} {}", inputStream, hasClassLoader);
        try {
            PMML commonPMMLModel = KiePMMLUtil.load(inputStream, fileName);
            Set<String> expectedClasses = commonPMMLModel.getModels()
                    .stream()
                    .map(model -> {
                        String modelPackageName = getSanitizedPackageName(String.format(PACKAGE_CLASS_TEMPLATE,
                                packageName,
                                model.getModelName()));
                        return modelPackageName + "." + getSanitizedClassName(model.getModelName());
                    })
                    .collect(Collectors.toSet());
            final List<KiePMMLModel> toReturn = getModelsWithSources(packageName, commonPMMLModel, hasClassLoader);
            final Set<String> generatedClasses = new HashSet<>();
            Map<String, Boolean> expectedClassModelTypeMap =
                    expectedClasses
                            .stream()
                            .collect(Collectors.toMap(expectedClass -> expectedClass,
                                    expectedClass -> {
                                        HasSourcesMap retrieved = getHasSourceMap(toReturn,
                                                expectedClass);
                                        generatedClasses.addAll(retrieved.getSourcesMap().keySet());
                                        return retrieved.isInterpreted();
                                    }));
            if (!generatedClasses.containsAll(expectedClasses)) {
                expectedClasses.removeAll(generatedClasses);
                String missingClasses = String.join(", ", expectedClasses);
                throw new KiePMMLException("Expected generated class " + missingClasses + " not found");
            }

            Map<String, String> factorySourceMap = getFactorySourceCode(factoryClassName, packageName, expectedClassModelTypeMap);
            KiePMMLFactoryModel kiePMMLFactoryModel = new KiePMMLFactoryModel(factoryClassName, packageName,
                    factorySourceMap);
            toReturn.add(kiePMMLFactoryModel);
            return toReturn;
        } catch (KiePMMLInternalException e) {
            throw new KiePMMLException("KiePMMLInternalException", e);
        } catch (KiePMMLException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalException("ExternalException", e);
        }
    }

    /**
     * Read the given <code>PMML</code> to returns a <code>List&lt;KiePMMLModel&gt;</code>
     *
     * @param packageName    the package into which put all the generated classes out of the given <code>PMML</code>
     * @param pmml
     * @param hasClassLoader
     * @return
     * @throws KiePMMLException if any <code>KiePMMLInternalException</code> has been thrown during execution
     */
    private List<Map<String, String>> getModelSourcesMap(final String packageName, final PMML pmml,
                                                         final HasClassLoader hasClassLoader) {
        logger.trace("getModelSourcesMap {}", pmml);
        return pmml
                .getModels()
                .stream()
                .map(model -> {
                    final CommonCompilationDTO<?> compilationDTO =
                            CommonCompilationDTO.fromGeneratedPackageNameAndFields(packageName, pmml, model,
                                    hasClassLoader);
                    return getSourcesMapFromCommonDataAndTransformationDictionaryAndModel(compilationDTO);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }


    /**
     * Read the given <code>PMML</code> to returns a <code>List&lt;KiePMMLModel&gt;</code>
     *
     * @param packageName    the package into which put all the generated classes out of the given <code>PMML</code>
     * @param pmml
     * @param hasClassLoader Using <code>HasClassloader</code>
     * @return
     * @throws KiePMMLException if any <code>KiePMMLInternalException</code> has been thrown during execution
     */
    private List<KiePMMLModel> getModels(final String packageName, final PMML pmml,
                                         final HasClassLoader hasClassLoader) {
        logger.trace("getModels {}", pmml);
        return pmml
                .getModels()
                .stream()
                .map(model -> {
                    final CommonCompilationDTO<?> compilationDTO =
                            CommonCompilationDTO.fromGeneratedPackageNameAndFields(packageName, pmml, model,
                                    hasClassLoader);
                    return getFromCommonDataAndTransformationDictionaryAndModel(compilationDTO);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Read the given <code>PMML</code> to returns a <code>List&lt;KiePMMLModel&gt;</code>
     *
     * @param packageName    the package into which put all the generated classes out of the given <code>PMML</code>
     * @param hasClassLoader Using <code>HasClassloader</code>
     * @return
     * @throws KiePMMLException if any <code>KiePMMLInternalException</code> has been thrown during execution
     */
    private List<KiePMMLModel> getModelsWithSources(final String packageName, final PMML pmml,
                                                    final HasClassLoader hasClassLoader) {
        logger.trace("getModels {}", pmml);
        return pmml
                .getModels()
                .stream()
                .map(model -> {
                    final CommonCompilationDTO<?> compilationDTO =
                            CommonCompilationDTO.fromGeneratedPackageNameAndFields(packageName, pmml, model,
                                    hasClassLoader);
                    return getFromCommonDataAndTransformationDictionaryAndModelWithSources(compilationDTO);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private HasSourcesMap getHasSourceMap(final List<KiePMMLModel> toReturn, final String expectedClass) {
        KiePMMLModel retrieved =
                toReturn.stream().filter(kiePMMLModel -> {
                            String fullSourceName =
                                    String.format(PACKAGE_CLASS_TEMPLATE,
                                            kiePMMLModel.getKModulePackageName(),
                                            getSanitizedClassName(kiePMMLModel.getName()));
                            return expectedClass.equals(fullSourceName);
                        })
                        .findFirst()
                        .orElseThrow(() -> new KiePMMLException(String.format("Expected KiePMMLModel %s not found",
                                expectedClass)));
        if (!(retrieved instanceof HasSourcesMap)) {
            throw new KiePMMLException(String.format("Expecting %s at this phase",
                    HasSourcesMap.class.getCanonicalName()));
        }
        return (HasSourcesMap) retrieved;
    }
}
