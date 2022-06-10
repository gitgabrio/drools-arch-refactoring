package org.kie.drl.compilation;/*
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
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.kie.dar.common.api.utils.FileUtils.getFileFromFileName;

class KieCompilerServiceDrlTest {

    private static KieCompilerService kieCompilerService;
    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        kieCompilerService = new KieCompilerServiceDrl();
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }


    @Test
    void canManageResource() throws IOException {
        Set<File> files = Files.list(Path.of("src/test/resources"))
                .map(Path::toFile)
                .filter(File::isFile)
                .collect(Collectors.toSet());
        DARResource toProcess = new DrlFileCollectionResource(files);
        // this is really only testing the constant field "drl" so it is always true...
        assertThat(kieCompilerService.canManageResource(toProcess)).isTrue();
        toProcess = () -> "DARRedirectOutput";
        assertThat(kieCompilerService.canManageResource(toProcess)).isFalse();
    }


    @Test
    void processResource() throws IOException {
        Set<File> files = Files.walk(Path.of("src/test/resources"))
                .map(Path::toFile)
                .filter(File::isFile)
                .collect(Collectors.toSet());
        DARResource toProcess = new DrlFileCollectionResource(files);
        List<DARCompilationOutput> retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
        assertThat(retrieved).isNotEmpty().hasSize(0);
//
////     TODO after enabling drools models
////        pmmlFile = getFileFromFileName("SimpleSetPredicateTree.pmml");
////        toProcess = new DARFileResource(pmmlFile);
////        retrieved = kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
////        assertThat(retrieved).isNotNull();
////        assertThat(retrieved instanceof DARRedirectOutputBar).isTrue();
////        assertThat(((DARRedirectOutputBar) retrieved).getTargetEngine()).isEqualTo("foo");
//
//        try {
//            toProcess = () -> "DARRedirectOutput";
//            kieCompilerService.processResource(toProcess, memoryCompilerClassLoader);
//            fail("Expecting KieCompilerServiceException");
//        } catch (Exception e) {
//            assertThat(e instanceof KieCompilerServiceException).isTrue();
//        }
    }

}