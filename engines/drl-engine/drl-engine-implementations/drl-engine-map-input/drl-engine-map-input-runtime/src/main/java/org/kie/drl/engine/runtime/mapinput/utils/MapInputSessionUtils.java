/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.drl.engine.runtime.mapinput.utils;

import org.drools.commands.impl.CommandFactoryServiceImpl;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.definition.type.FactType;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.dar.runtimemanager.api.model.DARMapInputDTO;
import org.kie.dar.runtimemanager.api.model.DAROriginalTypeGeneratedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.kie.dar.common.api.constants.Constants.OUTPUTFIELDS_MAP_IDENTIFIER;
import static org.kie.dar.common.api.constants.Constants.PACKAGE_CLASS_TEMPLATE;

/**
 * Class used to isolate all the <code>KieSession</code> instantiation/usage details
 */
public class MapInputSessionUtils {

    private static final Logger logger = LoggerFactory.getLogger(MapInputSessionUtils.class.getName());


    private static final CommandFactoryServiceImpl COMMAND_FACTORY_SERVICE = new CommandFactoryServiceImpl();
    final KieSession kieSession;
    final String modelName;
    final String packageName;
    final List<Command> commands;

    private MapInputSessionUtils(final KieSession kieSession, final String modelName, final String packageName, final DARMapInputDTO darMapInputDTO) {
        this.modelName = modelName;
        this.packageName = packageName;
        this.kieSession = kieSession;
        commands = new ArrayList<>();

        darMapInputDTO.getInserts().forEach(kieSession::insert);
        darMapInputDTO.getGlobals().forEach(kieSession::setGlobal);

//        darMapInputDTO.getInserts().forEach(toInsert -> commands.add(COMMAND_FACTORY_SERVICE.newInsert(toInsert)));
//        darMapInputDTO.getGlobals().forEach((key, value) -> commands.add(COMMAND_FACTORY_SERVICE.newSetGlobal(key, value)));
        addObjectsToSession(darMapInputDTO.getUnwrappedInputParams(), darMapInputDTO.getFieldTypeMap());
    }

    public static Builder builder(final KieSession kieSession, final String modelName, final String packageName, final DARMapInputDTO darMapInputDTO) {
        return new Builder(kieSession, modelName, packageName, darMapInputDTO);
    }

    /**
     * Invoke <code>KieSession.fireAllRules()</code>
     */
    public void fireAllRules() {
//        BatchExecutionCommand batchExecutionCommand = COMMAND_FACTORY_SERVICE.newBatchExecution(commands);
//        kieSession.execute(batchExecutionCommand);

        kieSession.fireAllRules();
        kieSession.dispose();
    }

    /**
     * Insert an <code>Object</code> to the underlying <code>KieSession</code>.
     *
     * @param toInsert   the <code>Object</code> to insert
     * @param globalName its global name
     */
    void insertObjectInSession(final Object toInsert, final String globalName) {
        commands.add(COMMAND_FACTORY_SERVICE.newInsert(toInsert));
        commands.add(COMMAND_FACTORY_SERVICE.newSetGlobal(globalName, toInsert));
    }

    /**
     * Add <code>Object</code>s to the underlying <code>KieSession</code>.
     * Such <code>Object</code>s are retrieved/instantiated from the given <code>Map</code>s and the content of the current kieSession' <code>KieBase</code>
     *
     * @param unwrappedInputParams
     * @param fieldTypeMap
     */
    private void addObjectsToSession(final Map<String, Object> unwrappedInputParams, final Map<String, DAROriginalTypeGeneratedType> fieldTypeMap) {
        for (Map.Entry<String, Object> entry : unwrappedInputParams.entrySet()) {
            if (!fieldTypeMap.containsKey(entry.getKey())) {
                throw new KieRuntimeServiceException(String.format("Field %s not mapped to generated type", entry.getKey()));
            }
            try {
                String generatedTypeName = fieldTypeMap.get(entry.getKey()).getGeneratedType();
                FactType factType = kieSession.getKieBase().getFactType(packageName, generatedTypeName);
                if (factType == null) {
                    String name = String.format(PACKAGE_CLASS_TEMPLATE, packageName, generatedTypeName);
                    String error = String.format("Failed to retrieve FactType %s for input value %s", name, entry.getKey());
                    throw new KieRuntimeServiceException(error);
                }
                Object toAdd = factType.newInstance();
                factType.set(toAdd, "value", entry.getValue());
                commands.add(COMMAND_FACTORY_SERVICE.newInsert(toAdd));
            } catch (Exception e) {
                throw new KieRuntimeServiceException(e.getMessage(), e);
            }
        }
    }

    public static class Builder {

        MapInputSessionUtils toBuild;

        private Builder(final KieSession kieSession, final String modelName, final String packageName, final DARMapInputDTO darMapInputDTO) {
            this.toBuild = new MapInputSessionUtils(kieSession, modelName, packageName, darMapInputDTO);
        }

        /**
         * Add an <code>AgendaEventListener</code> to the underlying <code>KieSession</code>
         *
         * @param agendaEventListener
         * @return
         */
        public Builder withAgendaEventListener(AgendaEventListener agendaEventListener) {
            this.toBuild.kieSession.addEventListener(agendaEventListener);
            return this;
        }

        /**
         * Insert <code>Map&lt;String, Object&gt;</code> <b>outputFieldsMap</b> to the underlying <code>KieSession</code>.
         * @param outputFieldsMap
         * @return
         */
        public Builder withOutputFieldsMap(final Map<String, Object> outputFieldsMap) {
            this.toBuild.insertObjectInSession(outputFieldsMap, OUTPUTFIELDS_MAP_IDENTIFIER);
            return this;
        }

        public MapInputSessionUtils build() {
            return this.toBuild;
        }
    }
}
