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
package org.kie.drl.engine.runtime.kiesession.local.utils;

import org.kie.api.runtime.KieSession;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.drl.engine.runtime.kiesession.local.model.DARInputDrlKieSessionLocal;
import org.kie.drl.engine.runtime.kiesession.local.model.DAROutputDrlKieSessionLocal;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.kie.dar.common.api.model.FRI.SLASH;
import static org.kie.dar.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedExecutableResource;
import static org.kie.drl.engine.runtime.utils.DARKieSessionUtil.loadKieSession;

public class DrlRuntimeHelper {

    private static final Logger logger = LoggerFactory.getLogger(DrlRuntimeHelper.class.getName());

    public static final String SUBPATH = "kiesessionlocal";
    public static final String PATH = "/drl" + SLASH + SUBPATH;


    private DrlRuntimeHelper() {
    }


    public static boolean canManage(FRI fri) {
        if (fri.getFri().startsWith(PATH)) {
            return getGeneratedExecutableResource(getCleanedFRI(fri), "drl").isPresent();
        } else {
            return false;
        }
    }

    public static Optional<DAROutputDrlKieSessionLocal> execute(DARInputDrlKieSessionLocal toEvaluate, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        KieSession kieSession;
        try {
            kieSession = loadKieSession(getCleanedFRI(toEvaluate.getFRI()), memoryCompilerClassLoader);
        } catch (Exception e) {
            logger.warn("{} can not execute {}",
                    DrlRuntimeHelper.class.getName(),
                    toEvaluate.getFRI());
            return Optional.empty();
        }
        if (kieSession == null) {
            return Optional.empty();
        }
        try {
            String sessionPath = toEvaluate.getFRI().getBasePath() + SLASH + kieSession.getIdentifier();
            FRI sessionFRI = new FRI(sessionPath, "drl");
            return Optional.of(new DAROutputDrlKieSessionLocal(sessionFRI, kieSession));
        } catch (Exception e) {
            throw new KieRuntimeServiceException(String.format("%s failed to execute %s",
                    DrlRuntimeHelper.class.getName(),
                    toEvaluate.getFRI()));
        }
    }

    static FRI getCleanedFRI(FRI originalFRI) {
        return new FRI(originalFRI.getBasePath().replace(SUBPATH + SLASH, ""), originalFRI.getModel());
    }

}
