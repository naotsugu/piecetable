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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link StringsBuffer}.
 * @author Naotsugu Kobayashi
 */
class StringsBufferTest {

    @Test
    void set() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb");
        assertEquals(3, sb.length());
        assertEquals(2, sb.rowSize());
    }


    @Test
    void append() {

        StringsBuffer sb = new StringsBuffer();

        sb.append("a");
        assertEquals(1, sb.length());
        assertEquals(1, sb.rowSize());

        sb.append("\n");
        assertEquals(2, sb.length());
        assertEquals(2, sb.rowSize());

        sb.append("b");
        assertEquals(3, sb.length());
        assertEquals(2, sb.rowSize());

        sb.append("\n");
        assertEquals(4, sb.length());
        assertEquals(3, sb.rowSize());
    }

    @Test
    void truncateRows() {

        StringsBuffer sb = new StringsBuffer();
        sb.truncateRows(1);
        assertEquals(0, sb.length());

        sb.set("""
            a
            b
            c""");
        assertEquals(3, sb.rowSize());
        sb.truncateRows(2);
        assertEquals(1, sb.rowSize());

        sb.set("""
            a
            b
            c
            """);
        assertEquals(4, sb.rowSize());
        sb.truncateRows(2);
        assertEquals(2, sb.rowSize());
    }

    @Test
    void insert() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("""
            a
            b
            c""");
        assertEquals(3, sb.rowSize());

        sb.insert(2, """
            1
            2
            """);

        assertEquals("""
            a
            1
            2
            b
            c""", sb.toString());
        assertEquals(5, sb.rowSize());
    }


    @Test
    void shiftAppend() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("""
            a
            b
            c
            """);
        assertEquals(4, sb.rowSize());

        sb.shiftAppend("""
            1
            2
            3
            """);
        assertEquals("""
            1
            2
            3
            """, sb.toString());
        assertEquals(4, sb.rowSize());

        sb.shiftAppend("""
            x
            y""");

        assertEquals("""
            2
            3
            x
            y""", sb.toString());
        assertEquals(4, sb.rowSize());

    }

    @Test
    void shiftInsert() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("""
            a
            b
            c
            """);
        assertEquals(4, sb.rowSize());

        sb.shiftInsert(0, """
            1
            2
            """);

        assertEquals("""
            1
            2
            a
            """, sb.toString());
        assertEquals(4, sb.rowSize());

    }
}
