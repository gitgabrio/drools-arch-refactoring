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
package org.kie.pmml.models.drools.commons.model;

import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.pmml.PMML4Result;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.common.api.model.GeneratedRedirectResource;
import org.kie.dar.runtimemanager.api.model.AbstractDARInput;
import org.kie.dar.runtimemanager.api.model.DARInput;
import org.kie.dar.runtimemanager.api.model.DARMapInputDTO;
import org.kie.dar.runtimemanager.api.model.DAROriginalTypeGeneratedType;
import org.kie.dar.runtimemanager.api.service.KieRuntimeService;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.enums.MINING_FUNCTION;
import org.kie.pmml.api.enums.PMML_MODEL;
import org.kie.pmml.api.enums.ResultCode;
import org.kie.pmml.api.runtime.PMMLContext;
import org.kie.pmml.commons.model.IsDrools;
import org.kie.pmml.commons.model.KiePMMLExtension;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.models.drools.executor.KiePMMLStatusHolder;
import org.kie.pmml.models.drools.tuples.KiePMMLOriginalTypeGeneratedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.kie.dar.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedRedirectResource;
import static org.kie.dar.runtimemanager.api.utils.SPIUtils.getKieRuntimeService;
import static org.kie.pmml.models.drools.commons.factories.KiePMMLDescrFactory.PMML4_RESULT_IDENTIFIER;
import static org.kie.pmml.models.drools.utils.KiePMMLAgendaListenerUtils.getAgendaEventListener;

/**
 * KIE representation of PMML model that use <b>drool</b> for implementation
 */
public abstract class KiePMMLDroolsModel extends KiePMMLModel implements IsDrools {

    private static final Logger logger = LoggerFactory.getLogger(KiePMMLDroolsModel.class);

    private static final AgendaEventListener agendaEventListener = getAgendaEventListener(logger);
    private static final long serialVersionUID = 5471400949048174357L;

    protected String kModulePackageName;

    /**
     * Map between the original field name and the generated type.
     */
    protected Map<String, KiePMMLOriginalTypeGeneratedType> fieldTypeMap = new HashMap<>();

    protected KiePMMLDroolsModel(final String modelName,
                                 final List<KiePMMLExtension> extensions) {
        super(modelName, extensions);
    }

    public Map<String, KiePMMLOriginalTypeGeneratedType> getFieldTypeMap() {
        return fieldTypeMap;
    }

    @Override
    public Object evaluate(final Map<String, Object> requestData,
                           final PMMLContext context) {
        logger.trace("evaluate {}", requestData);
        final PMML4Result toReturn = getPMML4Result(targetField);
        String fullClassName = this.getClass().getName();
        String packageName = fullClassName.contains(".") ?
                fullClassName.substring(0, fullClassName.lastIndexOf('.')) : "";

        List<Object> inserts = Arrays.asList(new KiePMMLStatusHolder(), toReturn);
        final Map<String, Object> globals = new HashMap<>();
        globals.put(PMML4_RESULT_IDENTIFIER, toReturn);


        Map<String, DAROriginalTypeGeneratedType> convertedFieldTypeMap = fieldTypeMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new DAROriginalTypeGeneratedType(entry.getValue().getOriginalType(),
                                entry.getValue().getOriginalType())));
        DARMapInputDTO darMapInputDTO = new DARMapInputDTO(inserts, globals, requestData, convertedFieldTypeMap);

        FRI fri = new FRI(this.getName(), "pmml");
        AbstractDARInput<DARMapInputDTO> input = new AbstractDARInput(fri, darMapInputDTO) {

        };
        // TODO retrieve runtimesercie
        // invoke runtimeservice.evaluate

        return toReturn;
    }

    @Override
    public String getKModulePackageName() {
        return kModulePackageName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KiePMMLDroolsModel.class.getSimpleName() + "[", "]")
                .add("kiePMMLOutputFields=" + kiePMMLOutputFields)
                .add("fieldTypeMap=" + fieldTypeMap)
                .add("pmmlMODEL=" + pmmlMODEL)
                .add("miningFunction=" + miningFunction)
                .add("targetField='" + targetField + "'")
                .add("name='" + name + "'")
                .add("extensions=" + extensions)
                .add("id='" + id + "'")
                .add("parentId='" + parentId + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KiePMMLDroolsModel that = (KiePMMLDroolsModel) o;
        return Objects.equals(kiePMMLOutputFields, that.kiePMMLOutputFields) &&
                Objects.equals(fieldTypeMap, that.fieldTypeMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kiePMMLOutputFields, fieldTypeMap);
    }

    private void evaluatePMML4Result(final PMML4Result pmml4Result, AbstractDARInput<DARMapInputDTO> toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        GeneratedRedirectResource redirectResource = getGeneratedRedirectResource(toEvaluate.getFRI(), "pmml").orElse(null);
        if (redirectResource == null) {
            logger.warn("{} can not redirect {}", KiePMMLDroolsModel.class.getName(), toEvaluate.getFRI());
            return;
        }
        FRI targetFri = new FRI(redirectResource.getFri().getBasePath(), redirectResource.getTarget());
        DARInput<DARMapInputDTO> redirectInput = new AbstractDARInput<>(targetFri, toEvaluate.getInputData()) {

        };

        Optional<KieRuntimeService> targetService = getKieRuntimeService(redirectInput, true, memoryCompilerClassLoader);
        if (targetService.isEmpty()) {
            logger.warn("Cannot find KieRuntimeService for {}", toEvaluate.getFRI());
            return;
        }
        targetService.get().evaluateInput(redirectInput, memoryCompilerClassLoader);
    }

    private PMML4Result getPMML4Result(final String targetField) {
        PMML4Result toReturn = new PMML4Result();
        toReturn.setResultCode(ResultCode.FAIL.getName());
        toReturn.setResultObjectName(targetField);
        return toReturn;
    }

    public abstract static class Builder<T extends KiePMMLDroolsModel> extends KiePMMLModel.Builder<T> {

        protected Builder(String prefix, PMML_MODEL pmmlMODEL, MINING_FUNCTION miningFunction, Supplier<T> supplier) {
            super(prefix, pmmlMODEL, miningFunction, supplier);
        }

        public Builder<T> withFieldTypeMap(Map<String, KiePMMLOriginalTypeGeneratedType> fieldTypeMap) {
            toBuild.fieldTypeMap = fieldTypeMap;
            return this;
        }

    }
}
