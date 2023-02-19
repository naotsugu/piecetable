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
package com.mammb.code.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenBufferAddTest {


    @Test void testAdd1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.prev(); sb.add("1");
        // 0: 1|aa
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            1aa""", sb.peekString(0, 3));
    }

    @Test void testAdd2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("2");
        // 0: a2|a
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals("""
            a2a""", sb.peekString(0, 3));
    }

    @Test void testAdd3() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("3");
        // 0: aa3|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            aa3""", sb.peekString(0, 3));
    }

    @Test void testAdd4() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa\n"); sb.prev(); sb.add("4");
        // 0: aa4|
        // 1: \n
        assertEquals(2, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            aa4
            """, sb.peekString(0, 4));
    }

    @Test void testAddLfLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.prev(); sb.add("\n");
        // 0: \n
        // 1:|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""

            aa""", sb.peekString(0, 3));
    }

    @Test void testAddLfCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("\n");
        // 0: a\n
        // 1:|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            a
            a""", sb.peekString(0, 3));
    }

    @Test void testAddLfRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("\n");
        // 0: aa\n
        // 1:|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            aa
            """, sb.peekString(0, 3));
    }

    @Test void testAddMultiLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("b\nb");
        // 0: b\n
        // 1: b|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            b
            baa""", sb.peekString(0, 5));
    }

    @Test void testAddMultiCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("b\nb");
        // 0: ab\n
        // 1: b|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            ab
            ba""", sb.peekString(0, 5));
    }

    @Test void testAddMultiRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("b\nb");
        // 0: aab\n
        // 1: b|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            aab
            b""", sb.peekString(0, 5));
    }

    @Test void testAddHalfLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("b\n");
        // 0: b\n
        // 1:|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            b
            aa""", sb.peekString(0, 4));
    }

    @Test void testAddHalfCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("b\n");
        // 0: ab\n
        // 1:|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            ab
            a""", sb.peekString(0, 4));
    }

    @Test void testAddHalfRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("b\n");
        // 0: aab\n
        // 1:|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            aab
            """, sb.peekString(0, 4));
    }

    @Test void testAddHalf2Left() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("\nb");
        // 0: \n
        // 1: b|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""

            baa""", sb.peekString(0, 4));
    }

    @Test void testAddHalf2Center() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("\nb");
        // 0: a\n
        // 1: b|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            a
            ba""", sb.peekString(0, 4));
    }

    @Test void testAddHalf2Right() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("\nb");
        // 0: aa\n
        // 1: b|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            aa
            b""", sb.peekString(0, 4));
    }


    @Test void testAddLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("b\ncc\nddd");
        // 0: b\n
        // 1: cc\n
        // 2: ddd|aa
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            b
            cc
            dddaa""", sb.peekString(0, 10));
    }

    @Test void testAddCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("b\ncc\nddd");
        // 0: ab\n
        // 1: cc\n
        // 2: ddd|a
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            ab
            cc
            ddda""", sb.peekString(0, 10));
    }

    @Test void testAddRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("b\ncc\nddd");
        // 0: aab\n
        // 1: cc\n
        // 2: ddd|
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            aab
            cc
            ddd""", sb.peekString(0, 10));
    }

    @Test void testAddMany1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(26);

        sb.add("""
            aaa
            bbb
            ccc""");

        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(11, sb.getCaretOffset());
    }

    @Test void testAddMany2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(26);

        sb.add("""
            aaa
            bbb
            ccc
            """);

        assertEquals(4, sb.rows.size());
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(12, sb.getCaretOffset());
    }

    @Test void testAddMany3() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(100);

        sb.add("123456789\n".repeat(50));
        assertEquals(51, sb.rows.size());
        assertEquals(50, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(500, sb.getCaretOffset());
    }

    @Test void testAddMany4() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(25);

        sb.add("""
            public class ScreenBuffer {

                /** Caret row on the text flow. */
                private int caretOffsetY = 0;
                /** Caret offset on the row. May be larger than the number of characters in a row. */
                private int caretOffsetX = 0;
                /** Offset on the text flow. */
                private IntegerProperty caretOffset = new SimpleIntegerProperty();
                /** screenRowSize. */
                private int screenRowSize = 1;
            """);
        sb.prevLine(); sb.prevLine(); sb.prevLine(); sb.prevLine(); sb.prevLine(); sb.prevLine();
        assertEquals(4, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("\n");
        assertEquals(5, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(103, sb.getCaretOffset());

        sb.nextLine(); sb.nextLine();
        sb.add("\n");
        assertEquals(8, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(228, sb.getCaretOffset());

        sb.nextLine(); sb.nextLine();
        sb.add("\n");
        assertEquals(11, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(336, sb.getCaretOffset());

    }

    @Test void testAddMany5() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(10);

        sb.add("aa\n".repeat(20) + "aa");
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretOffset());
    }


}
