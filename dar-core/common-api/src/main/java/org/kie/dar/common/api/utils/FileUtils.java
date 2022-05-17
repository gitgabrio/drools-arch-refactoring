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
package org.kie.dar.common.api.utils;

import org.kie.dar.common.api.exceptions.KieDARCommonException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class FileUtils {

    private FileUtils() {
    }

    public static InputStream getInputStreamFromFileName(String fileName) {
        try {
            InputStream toReturn = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            if (toReturn == null) {
                throw new KieDARCommonException(String.format("Failed to find %s", fileName));
            } else {
                return toReturn;
            }
        } catch (Exception e) {
            throw new KieDARCommonException(String.format("Failed to find %s due to %s", fileName,
                    e.getMessage()), e);
        }
    }

    public static File getFileFromFileName(String fileName) {
        try {
            URL retrieved = Thread.currentThread().getContextClassLoader().getResource(fileName);
            return new File(retrieved.getFile());
        } catch (Exception e) {
            throw new KieDARCommonException(String.format("Failed to find %s due to %s", fileName,
                    e.getMessage()), e);
        }
    }


}
