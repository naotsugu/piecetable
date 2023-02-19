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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScreenBufferDeleteTest {

    @Test
    void testDelete1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.home(); sb.delete(1);
        // 0:|bc
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretOffset());
        assertEquals("""
            bc""", sb.peekString(0, 2));
    }

    @Test
    void testDelete2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.prev(); sb.prev(); sb.delete(1);
        // 0: a|c
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals(1, sb.getCaretOffset());
        assertEquals("""
            ac""", sb.peekString(0, 2));
    }

    @Test
    void testDelete3() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.prev(); sb.delete(1);
        // 0: ab|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretOffset());
        assertEquals("""
            ab""", sb.peekString(0, 2));
    }

    @Test
    void testDelete4() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.delete(1);
        // 0: abc|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretOffset());
        assertEquals("""
            abc""", sb.peekString(0, 3));
    }

    @Test
    void testDeleteDouble1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.home(); sb.delete(2);
        // 0:|c
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretOffset());
        assertEquals("""
            c""", sb.peekString(0, 1));
    }

    @Test
    void testDeleteDouble2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.prev(); sb.prev(); sb.delete(2);
        // 0: a|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals(1, sb.getCaretOffset());
        assertEquals("""
            a""", sb.peekString(0, 1));
    }

    @Test
    void testDeleteDouble3() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.prev(); sb.delete(2);
        // 0: ab|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretOffset());
        assertEquals("""
            ab""", sb.peekString(0, 2));
    }

    @Test
    void testDeleteDouble4() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc"); sb.delete(2);
        // 0: abc|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretOffset());
        assertEquals("""
            abc""", sb.peekString(0, 3));
    }

    @Test
    void testDeleteDoubleLine1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc\ndef"); sb.prevLine(); sb.end(); sb.delete(1);
        // 0: abc|def
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretOffset());
        assertEquals("""
            abcdef""", sb.peekString(0, 6));
    }

    @Test
    void testDeleteDoubleLine2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc\ndef"); sb.prevLine(); sb.prev(); sb.delete(2);
        // 0: ab|def
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretOffset());
        assertEquals("""
            abdef""", sb.peekString(0, 5));
    }

    @Test
    void testDeleteDoubleLine3() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc\ndef"); sb.prevLine(); sb.delete(2);
        // 0: abc|ef
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretOffset());
        assertEquals("""
            abcef""", sb.peekString(0, 5));
    }

    @Test
    void testDeleteDoubleLine4() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc\ndef"); sb.prevLine(); sb.prev(); sb.delete(3);
        // 0: ab|ef
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretOffset());
        assertEquals("""
            abef""", sb.peekString(0, 4));
    }

    @Test
    void testDeleteDoubleLine5() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc\ndef\n"); sb.prevLine(); sb.home(); sb.delete(4);
        // 0: abc\n  ->  0: abc\n
        // 1: def\n      1:|
        // 2:|           2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(4, sb.getCaretOffset());
        assertEquals("""
            abc
            """, sb.peekString(0, 4));
    }

    @Test
    void testDeleteMultiLine1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("abc\ndef\nghi\n"); sb.prevLine(); sb.prevLine(); sb.delete(8);
        // 0: abc\n  ->  0: abc\n
        // 1: def\n      1:|
        // 2: ghi\n
        // 3:|
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(4, sb.getCaretOffset());
        assertEquals("""
            abc
            """, sb.peekString(0, 4));
    }

    @Test
    void testDeleteMultiLine1XXX() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(20);

        sb.add("""
            private int asBufferIndex(int pos) {

                if (pos < 0 || pos > pt.length()) {
                    throw new IllegalArgumentException("pos " + pos);
                }

                if (bufferHeadPos > pos || pos >= bufferTailPos) {
                    fillBuffer(pos);
                }

                return toIndex(pos);
            }""");
        assertEquals(11, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals(256, sb.getCaretOffset());

        sb.add("\n");
        assertEquals(12, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(257, sb.getCaretOffset());

        sb.prevLine();sb.prevLine();sb.prevLine();sb.prevLine();sb.prevLine();sb.prevLine();sb.prevLine();
        assertEquals(5, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(142, sb.getCaretOffset());

        sb.delete(1);
        assertEquals(5, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(142, sb.getCaretOffset());

        sb.delete(1);
        assertEquals(5, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(142, sb.getCaretOffset());

        sb.delete(85);
        assertEquals(5, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(142, sb.getCaretOffset());
        assertEquals("""
            private int asBufferIndex(int pos) {

                if (pos < 0 || pos > pt.length()) {
                    throw new IllegalArgumentException("pos " + pos);
                }

                return toIndex(pos);
            }
            """, sb.peekString(0, 170));
    }

}
