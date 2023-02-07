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
package com.mammb.code.editor3.lang;

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

    /**
     * Returns the number of Unicode code points in the specified text.
     * @param cs the specified text
     * @return the number of Unicode code points
     */
    public static int codePointCount(CharSequence cs) {
        return (cs == null) ? 0 : codePointCount(cs, 0, cs.length());
    }

    /**
     * Returns the number of Unicode code points in the specified text range of this String.
     * @param cs the specified text
     * @param beginIndex the index to the first char of the text range
     * @param endIndex the index after the last char of the text range
     * @return the number of Unicode code points
     */
    public static int codePointCount(CharSequence cs, int beginIndex, int endIndex) {
        return (cs == null) ? 0 : (int) cs.subSequence(beginIndex, endIndex).codePoints().count();
    }

}
