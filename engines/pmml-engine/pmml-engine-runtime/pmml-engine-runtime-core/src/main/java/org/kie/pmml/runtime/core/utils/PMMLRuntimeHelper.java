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
package org.kie.pmml.runtime.core.utils;

import org.kie.api.pmml.PMML4Result;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedExecutableResource;
import org.kie.dar.common.api.model.GeneratedRedirectResource;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.enums.PMML_MODEL;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.kie.pmml.api.runtime.PMMLContext;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.commons.model.KiePMMLModelFactory;
import org.kie.pmml.commons.model.ProcessingDTO;
import org.kie.pmml.runtime.core.executor.PMMLModelEvaluator;
import org.kie.pmml.runtime.core.executor.PMMLModelEvaluatorFinder;
import org.kie.pmml.runtime.core.executor.PMMLModelEvaluatorFinderImpl;
import org.kie.pmml.runtime.core.model.DARInputPMML;
import org.kie.pmml.runtime.core.model.DAROutputPMML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.kie.dar.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedExecutableResource;
import static org.kie.dar.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedRedirectResource;
import static org.kie.dar.runtimemanager.api.utils.SPIUtils.getKieRuntimeService;
import static org.kie.pmml.runtime.core.utils.PostProcess.postProcess;
import static org.kie.pmml.runtime.core.utils.PreProcess.preProcess;

public class PMMLRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(PMMLRuntimeHelper.class.getName());
    private static final PMMLModelEvaluatorFinder pmmlModelExecutorFinder = new PMMLModelEvaluatorFinderImpl();


    private PMMLRuntimeHelper() {
    }


    public static boolean canManage(FRI fri) {
        return Stream.of(getGeneratedExecutableResource(fri), getGeneratedRedirectResource(fri))
                .anyMatch(Optional::isPresent);
    }

    public static Optional<DAROutputPMML> execute(DARInputPMML toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        KiePMMLModelFactory kiePMMLModelFactory;
        try {
            kiePMMLModelFactory = loadKiePMMLModelFactory(toEvaluate.getFRI(), memoryCompilerClassLoader);
        } catch (Exception e) {
            logger.warn("{} can not execute {}",
                    PMMLRuntimeHelper.class.getName(),
                    toEvaluate.getFRI());
            return Optional.empty();
        }
        try {
            return Optional.of(getDAROutput(kiePMMLModelFactory, toEvaluate));
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s failed to execute %s",
                    PMMLRuntimeHelper.class.getName(),
                    toEvaluate.getFRI()), e);
        }
    }

    public static Optional<DAROutputPMML> redirect(DARInputPMML toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedRedirectResource redirectResource = getGeneratedRedirectResource(toEvaluate.getFRI()).orElse(null);
        if (redirectResource == null) {
            logger.warn("{} can not redirect {}", PMMLRuntimeHelper.class.getName(), toEvaluate.getFRI());
            return Optional.empty();
        }
        FRI targetFri = new FRI(redirectResource.getFri().getBasePath(), redirectResource.getTarget());
        DARInput redirectInput = new DARInputPMML(targetFri, toEvaluate.getInputData()); // TODO fix for drools models

        Optional<KieRuntimeService> targetService = getKieRuntimeService(redirectInput.getFRI(), true, memoryCompilerClassLoader);
        if (targetService.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", toEvaluate.getFRI());
            return Optional.empty();
        }
        return targetService.map(service -> service.evaluateInput(redirectInput, memoryCompilerClassLoader))
                .map(o -> new DAROutputPMML(toEvaluate.getFRI(), null)); // TODO fix for drools models));
    }

    @SuppressWarnings("unchecked")
    static KiePMMLModelFactory loadKiePMMLModelFactory(FRI fri, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedExecutableResource finalResource = getGeneratedExecutableResource(fri)
                .orElseThrow(() -> new KieRuntimeServiceException("Can not find expected GeneratedExecutableResource for " + fri));
        try {
            String fullKiePMMLModelFactorySourceClassName = finalResource.getFullClassNames().get(0);
            final Class<? extends KiePMMLModelFactory> aClass =
                    (Class<? extends KiePMMLModelFactory>) memoryCompilerClassLoader.loadClass(fullKiePMMLModelFactorySourceClassName);
            return aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new KieRuntimeServiceException(e);
        }
    }

    static DAROutputPMML getDAROutput(KiePMMLModelFactory kiePMMLModelFactory, DARInputPMML darInputPMML) {
        List<KiePMMLModel> kiePMMLModels = kiePMMLModelFactory.getKiePMMLModels();
        PMML4Result result = evaluate(kiePMMLModels, darInputPMML.getInputData());
        return new DAROutputPMML(darInputPMML.getFRI(), result);
    }

    static PMML4Result evaluate(final List<KiePMMLModel> kiePMMLModels, final PMMLContext pmmlContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("evaluate {}", pmmlContext);
        }
        String modelName = pmmlContext.getRequestData().getModelName();
        KiePMMLModel toEvaluate = getModel(kiePMMLModels, modelName).orElseThrow(() -> new KiePMMLException("Failed to retrieve model with name " + modelName));
        return evaluate(toEvaluate, pmmlContext);
    }

    public static PMML4Result evaluate(final KiePMMLModel model, final PMMLContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("evaluate {} {}", model, context);
        }
//        pmmlListeners.forEach(context::addPMMLListener);
//        addStep(() -> getStep(START, model, context.getRequestData()), context);
        final ProcessingDTO processingDTO = preProcess(model, context);
//        addStep(() -> getStep(PRE_EVALUATION, model, context.getRequestData()), context);
        PMMLModelEvaluator executor = getFromPMMLModelType(model.getPmmlMODEL())
                .orElseThrow(() -> new KiePMMLException(String.format("PMMLModelEvaluator not found for model %s",
                        model.getPmmlMODEL())));
        PMML4Result toReturn = executor.evaluate(model, context);
//        addStep(() -> getStep(POST_EVALUATION, model, context.getRequestData()), context);
        postProcess(toReturn, model, context, processingDTO);
//        addStep(() -> getStep(END, model, context.getRequestData()), context);
        return toReturn;
    }

    static Optional<KiePMMLModel> getModel(final List<KiePMMLModel> kiePMMLModels, String modelName) {
        logger.trace("getModel {} {}", kiePMMLModels, modelName);
        return kiePMMLModels
                .stream()
                .filter(model -> Objects.equals(modelName, model.getName()))
                .findFirst();
    }

    /**
     * Returns an <code>Optional&lt;PMMLModelExecutor&gt;</code> to allow
     * incremental development of different model-specific executors
     *
     * @param pmmlMODEL
     * @return
     */
    private static Optional<PMMLModelEvaluator> getFromPMMLModelType(final PMML_MODEL pmmlMODEL) {
        logger.trace("getFromPMMLModelType {}", pmmlMODEL);
        return pmmlModelExecutorFinder.getImplementations(false)
                .stream()
                .filter(implementation -> pmmlMODEL.equals(implementation.getPMMLModelType()))
                .findFirst();
    }
}
