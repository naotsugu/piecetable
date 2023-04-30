/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.lang;

import java.nio.file.Path;

/**
 * The path utilities.
 * @author Naotsugu Kobayashi
 */
public class Paths {

    /**
     * Get the extension name.
     * @param path the source path
     * @return the extension name
     */
    public static String getExtension(Path path) {
        if (path == null) {
            return "";
        }
        String fileName = path.getFileName().toString();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }


    /**
     * Get the extension name.
     * @param path the source path
     * @return the extension name
     */
    public static String getExtensionExact(Path path) {
        if (path == null || !path.toFile().isFile()) {
            return "";
        }
        return getExtension(path);
    }

}

