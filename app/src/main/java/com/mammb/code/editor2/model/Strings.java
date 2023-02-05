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
package com.mammb.code.editor2.model;

/**
 * The string utilities.
 * @author Naotsugu Kobayashi
 */
public class Strings {

    /** carriage return. */
    public static char CR = '\r';
    /** line feed. */
    public static char LF = '\n';

    /**
     * Count the number of line feed in the specified char sequence.
     * @param cs the specified char sequence
     * @return the number of line feed
     */
    public static int countLf(CharSequence cs) {
        return (int) cs.chars().filter(c -> c == LF).count();
    }

}
