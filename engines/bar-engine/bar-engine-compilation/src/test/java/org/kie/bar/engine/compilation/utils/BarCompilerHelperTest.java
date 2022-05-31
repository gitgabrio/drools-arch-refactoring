package org.kie.bar.engine.compilation.utils;/*
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
import org.kie.bar.engine.compilation.model.DARFinalOutputBar;
import org.kie.bar.engine.compilation.model.DARRedirectOutputBar;
import org.kie.dar.compilationmanager.api.model.DARFileResource;
import org.kie.memorycompiler.KieMemoryCompiler;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.bar.engine.compilation.TestingUtils.getFileFromFileName;

class BarCompilerHelperTest {

    private static KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader;

    @BeforeAll
    static void setUp() {
        memoryCompilerClassLoader = new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Test
    void getDARFinalOutputBar() {
        File barFile = getFileFromFileName("RedirectBar.bar");
        DARFileResource darResourceBar = new DARFileResource(barFile);
        DARFinalOutputBar retrieved = BarCompilerHelper.getDARFinalOutputBar(darResourceBar, memoryCompilerClassLoader);
        assertNotNull(retrieved);

    }

    @Test
    void getDARRedirectOutputBar() {
        File fooFile = getFileFromFileName("DarBar.bar");
        DARFileResource darResourceBar = new DARFileResource(fooFile);
        DARRedirectOutputBar retrieved = BarCompilerHelper.getDARRedirectOutputBar(darResourceBar);
        assertNotNull(retrieved);
        assertEquals("foo", retrieved.getTargetEngine());
    }


}