package org.kie.pmml.compiler.service;/*
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;

class KieCompilerServicePMMLTest {

    private static KieCompilerService kieCompilerService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieCompilerService = new KieCompilerServicePMML();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }


    @Test
    void canManageResource() {
        File pmmlFile = getFileFromFileName("LinearRegressionSample.pmml");
        DARResource toProcess = new DARFileResource(pmmlFile);
        assertThat(kieCompilerService.canManageResource(toProcess)).isTrue();
        toProcess = () -> "DARRedirectOutput";
        assertThat(kieCompilerService.canManageResource(toProcess)).isFalse();
    }

//    @Test
//    void processResource() {
//        File pmmlFile = getFileFromFileName("TestingSample.pmml");
//        DARResource toProcess = new DARFileResource(pmmlFile);
//        DARCompilationOutput retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
//        assertThat(retrieved).isNotNull().isInstanceOf(DARCallableOutputPMML.class);

        // TODO after enabling drools models
//        pmmlFile = getFileFromFileName("SimpleSetPredicateTree.pmml");
//        toProcess = new DARFileResource(pmmlFile);
//        retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
//        assertThat(retrieved).isNotNull();
//        assertThat(retrieved instanceof DARRedirectOutputBar).isTrue();
//        assertThat(((DARRedirectOutputBar) retrieved).getTargetEngine()).isEqualTo("foo");

//        try {
//            toProcess = () -> "DARRedirectOutput";
//            kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
//            fail("Expecting KieCompilerServiceException");
//        } catch (Exception e) {
//            assertThat(e instanceof KieCompilerServiceException).isTrue();
//        }
//    }
}