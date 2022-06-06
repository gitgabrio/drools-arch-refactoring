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
package org.kie.dar.common.utils;

public class StringUtils {

    private static final String FOLDER_SEPARATOR = "/";

    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    private StringUtils() {
    }

    /**
     * Method to be used by <b>every</b> KiePMML implementation to retrieve the <b>package</b> name
     * out of the model name
     *
     * @param modelName
     * @return
     */
    public static String getSanitizedPackageName(String modelName) {
        return modelName.replaceAll("[^A-Za-z0-9.]", "").toLowerCase();
    }

    /**
     * Convert the given <code>String</code> in a valid class name (i.e. no dots, no spaces, first letter upper case)
     *
     * @param input
     * @return
     */
    public static String getSanitizedClassName(String input) {
        String upperCasedInput = input.substring(0, 1).toUpperCase() + input.substring(1);
        return upperCasedInput.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Returns an array where the first item is the <b>factory class</b> name and the second item is the <b>package</b> name,
     * built starting from the given <b>sourcePath</b> <code>String</code>
     * @param sourcePath
     * @return
     */
    public static String[] getFactoryClassNamePackageName(String sourcePath) {
        sourcePath = sourcePath.replace(WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);
        String fileName = sourcePath.substring(sourcePath.lastIndexOf(FOLDER_SEPARATOR) + 1);
        fileName = fileName.replace(".pmml", "");
        String packageName = getSanitizedPackageName(fileName);
        String factoryClassName = getSanitizedClassName(fileName + "Factory");
        return new String[]{factoryClassName, packageName};
    }



}
