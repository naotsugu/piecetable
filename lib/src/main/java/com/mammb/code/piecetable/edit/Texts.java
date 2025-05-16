/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.piecetable.edit;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for processing and manipulating text.
 * @author Naotsugu Kobayashi
 */
class Texts {

    /**
     * Gets a new {@code String} composed of copies of the
     * {@code CharSequence elements} joined together.
     * <pre>
     *     ["abc", "def", "ghi"]  - join -> "abcdefghi"
     * </pre>
     * @param elements an {@code Iterable} that will have its {@code elements} joined together.
     * @return a new {@code String} that is composed of the {@code elements} argument
     */
    static String join(Iterable<? extends CharSequence> elements) {
        return String.join("", elements);
    }

    /**
     * Get a count of row break on the specified text.
     * <pre>
     *     "***\r\n***\r\n***"  ->  2
     *
     *     "***\n***\n***"      ->  2
     * </pre>
     * @param text the specified text
     * @return a count of row break
     */
    static int countRowBreak(String text) {
        return (int) text.chars().filter(c -> c == '\n').count();
    }

    /**
     * Splits the specified string with a row break symbol.
     * <pre>
     *     "ab\ncd\n"  ->  ["ab\n", "cd\n", ""]
     * </pre>
     * @param text the specified text
     * @return the list of split strings
     */
    static List<String> splitRowBreak(String text) {
        if (text == null) return List.of("");
        List<String> list = new ArrayList<>();
        for (int begin = 0; begin <= text.length();) {
            int end = text.indexOf('\n', begin) + 1;
            if (end > 0) {
                list.add(text.substring(begin, end));
                begin = end;
            } else {
                list.add(text.substring(begin));
                break;
            }
        }
        return list;
    }

    /**
     * Gets the count of the characters on the display.
     * Surrogate pairs and CR LF, count as one character.
     * Ligatures are not taken into consideration.
     * @param text the specified text
     * @return the count of the characters on the display
     */
    static int chLength(String text) {
        int chLen = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            chLen += countForward(ch);
        }
        return chLen;
    }


    /**
     * Gets the leftmost count characters of a text.
     * <pre>
     *     | * | * | h | l | * | \r | \n | * | * |
     *       1   2       3            4    5
     *   h: high surrogate
     *   l: low surrogate
     * </pre>
     *
     * @param text the text
     * @param count the count
     * @return the leftmost count characters of a text
     */
    static String left(String text, int count) {
        if (count <= 0) return "";
        char[] ca = text.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            count -= countForward(ca[i]);
            if (count <= 0) {
                return new String(ca, 0, i + 1);
            }
        }
        return text;
    }


    /**
     * Gets the rightmost count characters of a text.
     * <pre>
     *     | * | * | h | l | * | \r | \n | * | * |
     *               5       4   3         2   1
     *   h: high surrogate
     *   l: low surrogate
     * </pre>
     * @param text the text
     * @param count the count
     * @return the rightmost count characters of a text
     */
    static String right(String text, int count) {
        if (count <= 0) return "";
        char[] ca = text.toCharArray();
        for (int i = ca.length - 1; i >= 0; i--) {
            char next = ((i - 1) >= 0) ? ca[i - 1] : 0;
            count -= countBackward(ca[i], next);
            if (count <= 0) {
                return new String(ca, i, ca.length - i);
            }
        }
        return text;
    }

    /**
     * <pre>
     *     | h | l | * | \r | \n |
     *       ^   ^   ^   ^    ^
     *       0   1   1   0    1
     *   h: high surrogate
     *   l: low surrogate
     * </pre>
     * @param c the character
     * @return the count of character
     */
    private static int countForward(char c) {
        return (Character.isHighSurrogate(c) || c == '\r') ? 0 : 1;
    }

    /**
     * <pre>
     *     | h | l | * | \r | \n |
     *       ^   ^   ^   ^    ^
     *       1   0   1   1    0
     *   h: high surrogate
     *   l: low surrogate
     * </pre>
     * <pre>
     *  | c2 | c1 |
     *  | \r | \n |
     *  |    | \n |
     * </pre>
     *
     * @param c1 the right character
     * @param c2 the left character
     * @return the count of character
     */
    private static int countBackward(char c1, char c2) {
        return (Character.isLowSurrogate(c1) || (c1 == '\n' && c2 == '\r')) ? 0 : 1;
    }

}
