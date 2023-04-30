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

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link Strings}.
 * @author Naotsugu Kobayashi
 */
class StringsTest {

    @Test void lengthOfLastRow() {
        assertEquals(0, Strings.lengthOfLastRow(""));
        assertEquals(1, Strings.lengthOfLastRow("a"));
        assertEquals(2, Strings.lengthOfLastRow("ab"));
        assertEquals(4, Strings.lengthOfLastRow("a😀b"));

        assertEquals(0, Strings.lengthOfLastRow("\n"));
        assertEquals(0, Strings.lengthOfLastRow("a\n"));
        assertEquals(0, Strings.lengthOfLastRow("ab\n"));
        assertEquals(0, Strings.lengthOfLastRow("a😀b\n"));

        assertEquals(1, Strings.lengthOfLastRow("\na"));
        assertEquals(1, Strings.lengthOfLastRow("a\nb"));
        assertEquals(2, Strings.lengthOfLastRow("ab\ncd"));
        assertEquals(3, Strings.lengthOfLastRow("a😀b\nefg"));
    }

    @Test void lastIndexOf() {
        assertEquals(-1, Strings.lastIndexOf("", 'a'));
        assertEquals(0, Strings.lastIndexOf("abc", 'a'));
        assertEquals(1, Strings.lastIndexOf("abc", 'b'));
        assertEquals(2, Strings.lastIndexOf("abc", 'c'));
        assertEquals(-1, Strings.lastIndexOf("abc", 'x'));
    }

    @Test void countRow() {
        assertEquals(0, Strings.countRow(""));
        assertEquals(1, Strings.countRow("a"));
        assertEquals(1, Strings.countRow("a\n"));
        assertEquals(2, Strings.countRow("a\nb"));
        assertEquals(2, Strings.countRow("a\nb\n"));
        assertEquals(3, Strings.countRow("a\nb\nc"));
        assertEquals(3, Strings.countRow("a\nb\nc\n"));
    }

    @Test void testCountLf() {
        assertEquals(0, Strings.countLf(""));
        assertEquals(0, Strings.countLf("a"));
        assertEquals(0, Strings.countLf("ab"));

        assertEquals(1, Strings.countLf("\n"));
        assertEquals(1, Strings.countLf("\na"));
        assertEquals(1, Strings.countLf("a\n"));

        assertEquals(2, Strings.countLf("\n\n"));
        assertEquals(2, Strings.countLf("\na\n"));
        assertEquals(2, Strings.countLf("a\n\n"));
        assertEquals(2, Strings.countLf("a\nb\n"));
        assertEquals(2, Strings.countLf("a\nb\nc"));
    }

    @Test void unifyLf() {
        assertEquals("", Strings.unifyLf(""));
        assertEquals("a", Strings.unifyLf("a"));
        assertEquals("ab", Strings.unifyLf("ab"));
        assertEquals("\n", Strings.unifyLf("\n"));
        assertEquals("\n", Strings.unifyLf("\r\n"));
        assertEquals("\n\n", Strings.unifyLf("\r\n\r\n"));
        assertEquals("a\nb\nc", Strings.unifyLf("a\nb\r\nc"));
    }

    @Test void unifyCrLf() {
        assertEquals("", Strings.unifyCrLf(""));
        assertEquals("a", Strings.unifyCrLf("a"));
        assertEquals("ab", Strings.unifyCrLf("ab"));
        assertEquals("\r\n", Strings.unifyCrLf("\r\n"));
        assertEquals("\r\n", Strings.unifyCrLf("\n"));
        assertEquals("\r\n\r\n", Strings.unifyCrLf("\n\n"));
        assertEquals("a\r\nb\r\nc", Strings.unifyCrLf("a\nb\r\nc"));
    }

    @Test void lengthByteAsUtf16() {
        assertEquals(0, Strings.lengthByteAsUtf16((byte) -1));
        assertEquals(1, Strings.lengthByteAsUtf16("a".getBytes(StandardCharsets.UTF_8)[0]));
        assertEquals(1, Strings.lengthByteAsUtf16("あ".getBytes(StandardCharsets.UTF_8)[0]));
        assertEquals(2, Strings.lengthByteAsUtf16("😀".getBytes(StandardCharsets.UTF_8)[0]));
    }

}
