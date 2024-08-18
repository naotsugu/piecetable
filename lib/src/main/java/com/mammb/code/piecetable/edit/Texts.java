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

/**
 * The text utility.
 * @author Naotsugu Kobayashi
 */
class Texts {

    static String join(Iterable<? extends CharSequence> elements) {
        return String.join("", elements);
    }

    static int countRowBreak(String str) {
        return (int) str.chars().filter(c -> c == '\n').count();
    }

    static int leftCol(String str) {
        int index = str.indexOf('\n');
        return index < 0 ? str.length() : index + 1;
    }

    static int chLength(String str) {
        int chLen = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            chLen += chCountLeft(ch);
        }
        return chLen;
    }

    static String chLeft(String str, int chLen) {
        if (chLen == 0) return "";
        char[] ca = str.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            chLen -= chCountLeft(ca[i]);
            if (chLen <= 0) {
                return new String(ca, 0, i + 1);
            }
        }
        return str;
    }

    static String chRight(String str, int chLen) {
        if (chLen == 0) return "";
        char[] ca = str.toCharArray();
        for (int i = ca.length - 1; i >= 0; i--) {
            char next = ((i - 1) >= 0) ? ca[i - 1] : 0;
            chLen -= chCountRight(ca[i], next);
            if (chLen <= 0) {
                return new String(ca, i, ca.length - i);
            }
        }
        return str;
    }

    private static int chCountLeft(char c) {
        return (Character.isHighSurrogate(c) || c == '\r') ? 0 : 1;
    }

    // | c2 | c1 |
    // | \r | \n |
    // |    | \n |
    private static int chCountRight(char c1, char c2) {
        return (Character.isLowSurrogate(c1) || (c1 == '\n' && c2 == '\r')) ? 0 : 1;
    }

}
