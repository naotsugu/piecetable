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
package com.mammb.code.editor.lang;

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

        sb.set("");
        assertEquals(0, sb.length());
        assertEquals(1, sb.rowViewSize());

        sb.set("a");
        assertEquals(1, sb.length());
        assertEquals(1, sb.rowViewSize());

        sb.set("ab");
        assertEquals(2, sb.length());
        assertEquals(1, sb.rowViewSize());

        sb.set("a\n");
        assertEquals(2, sb.length());
        assertEquals(2, sb.rowViewSize());

        sb.set("a\nb");
        assertEquals(3, sb.length());
        assertEquals(2, sb.rowViewSize());

        sb.set("\n");
        assertEquals(1, sb.length());
        assertEquals(2, sb.rowViewSize());
    }


    @Test
    void append() {

        StringsBuffer sb = new StringsBuffer();

        // [a] + [b] = [ab]
        sb.set("");
        sb.append("a");
        sb.append("b");
        assertEquals(2, sb.length());
        assertEquals(1, sb.rowViewSize());

        // [ab] + [$c] = [ab$
        //                c]
        sb.set("");
        sb.append("ab");
        sb.append("\nc");
        assertEquals(4, sb.length());
        assertEquals(2, sb.rowViewSize());

        // [a$] + [b] = [a$
        //               b]
        sb.set("");
        sb.append("a\n");
        sb.append("b");
        assertEquals(3, sb.length());
        assertEquals(2, sb.rowViewSize());

        // [a$] + [b$] = [a$
        //                b$
        //               ]
        sb.set("");
        sb.append("a\n");
        sb.append("b\n");
        assertEquals(4, sb.length());
        assertEquals(3, sb.rowViewSize());

        // [a] + [$] = [a$ + [b] = [a$  + [$]  = [a$
        //             ]            b]            b$
        //                                       ]
        sb.set("");
        sb.append("a");
        assertEquals(1, sb.length());
        assertEquals(1, sb.rowViewSize());
        sb.append("\n");
        assertEquals(2, sb.length());
        assertEquals(2, sb.rowViewSize());
        sb.append("b");
        assertEquals(3, sb.length());
        assertEquals(2, sb.rowViewSize());
        sb.append("\n");
        assertEquals(4, sb.length());
        assertEquals(3, sb.rowViewSize());
    }


    @Test
    void insert() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("""
            a
            b
            c""");
        assertEquals(3, sb.rowViewSize());

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
        assertEquals(5, sb.rowViewSize());

        sb.insert(9, """
            x
            y""");
        assertEquals("""
            a
            1
            2
            b
            cx
            y""", sb.toString());
        assertEquals(6, sb.rowViewSize());

    }


    @Test
    void delete() {
        StringsBuffer sb = new StringsBuffer();
        sb.append("""
            a
            b
            c
            """);
        assertEquals(4, sb.rowViewSize());

        sb.delete(4, 2); // [c$]
        assertEquals("""
            a
            b
            """, sb.toString());
        assertEquals(3, sb.rowViewSize());

        sb.delete(3, 1); // [$]
        assertEquals("""
            a
            b""", sb.toString());
        assertEquals(2, sb.rowViewSize());

        sb.delete(0, 10); // all
        assertEquals("", sb.toString());
        assertEquals(1, sb.rowViewSize());

    }


    @Test
    void shiftAppend() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("""
            a
            b
            c
            """);
        assertEquals(4, sb.rowViewSize());

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
        assertEquals(4, sb.rowViewSize());

        sb.shiftAppend("""
            x
            y""");

        assertEquals("""
            2
            3
            x
            y""", sb.toString());
        assertEquals(4, sb.rowViewSize());

    }

    @Test
    void shiftInsert() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("""
            a
            b
            c
            """);
        assertEquals(4, sb.rowViewSize());

        sb.shiftInsert(0, """
            1
            2
            """, 3);

        assertEquals("""
            1
            2
            a
            """, sb.toString());
        assertEquals(4, sb.rowViewSize());

    }

    /**
     * Trim the rows.
     * <pre>
     *    1:  a$  -- max:2 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3: |
     *    4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     * </pre>
     */
    @Test void trim1() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\n");
        assertEquals(4, sb.rowViewSize());

        sb.trim(2);
        assertEquals("a\nb\n", sb.toString());
        assertEquals(3, sb.rowViewSize());
    }


    /**
     * Trim the rows.
     * <pre>
     *    1:  a$  -- max:3 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3:  c$
     *    4: |                  4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 4
     * </pre>
     */
    @Test void trim2() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\n");
        assertEquals(4, sb.rowViewSize());

        sb.trim(3);
        assertEquals("a\nb\nc\n", sb.toString());
        assertEquals(4, sb.rowViewSize());
    }


    /**
     * Trim the rows.
     * <pre>
     *    1:  a$  -- max:3 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3:  c$
     *    4:  d                 4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 4
     * </pre>
     */
    @Test void trim3() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\nd");
        assertEquals(4, sb.rowViewSize());

        sb.trim(3);
        assertEquals("a\nb\nc\n", sb.toString());
        assertEquals(4, sb.rowViewSize());
    }


    /**
     * Trim the rows.
     * <pre>
     *    1:  a$  -- max:2 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3: |
     *    4:  d                 4:
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     * </pre>
     */
    @Test void trim4() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\nd");
        assertEquals(4, sb.rowViewSize());

        sb.trim(2);
        assertEquals("a\nb\n", sb.toString());
        assertEquals(3, sb.rowViewSize());
    }


    /**
     * <pre>
     *    1:  a$  -- max:2 -->  2:  b$
     *    2:  b$                3:  c$
     *    3:  c$                4: |
     *    4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     * </pre>
     */
    @Test void trimBefore1() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\n");
        assertEquals(4, sb.rowViewSize());

        sb.trimBefore(2);
        assertEquals("b\nc\n", sb.toString());
        assertEquals(3, sb.rowViewSize());
    }


    /**
     * <pre>
     *    1:  a$  -- max:3 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3:  c$
     *    4: |                  4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 4
     * </pre>
     */
    @Test void trimBefore2() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\n");
        assertEquals(4, sb.rowViewSize());

        sb.trimBefore(3);
        assertEquals("a\nb\nc\n", sb.toString());
        assertEquals(4, sb.rowViewSize());
    }


    /**
     * <pre>
     *    1:  a$  -- max:3 -->  2:  b$
     *    2:  b$                3:  c$
     *    3:  c$                4:  d|
     *    4:  d|
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     * </pre>
     */
    @Test void trimBefore3() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\nd");
        assertEquals(4, sb.rowViewSize());

        sb.trimBefore(3);
        assertEquals("b\nc\nd", sb.toString());
        assertEquals(3, sb.rowViewSize());
    }


    /**
     * <pre>
     *    1:  a$  -- max:2 -->  3:  c$
     *    2:  b$                4:  d|
     *    3:  c$
     *    4:  d
     *  ---------------------------------
     *   rowSize 4            rowSize 2
     * </pre>
     */
    @Test void trimBefore4() {
        StringsBuffer sb = new StringsBuffer();
        sb.set("a\nb\nc\nd");
        assertEquals(4, sb.rowViewSize());

        sb.trimBefore(2);
        assertEquals("c\nd", sb.toString());
        assertEquals(2, sb.rowViewSize());
    }

}
