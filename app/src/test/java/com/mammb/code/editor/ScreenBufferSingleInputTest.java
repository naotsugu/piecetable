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

class ScreenBufferSingleInputTest {

    @Test void test() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa");
        // 0: aa|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretIndex());

        sb.add("\n");
        // 0: aa\n
        // 1:│
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretIndex());

        sb.add("\n");
        // 0: aa\n
        // 1: \n
        // 2:│
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(4, sb.getCaretIndex());

        sb.prevLine();
        // 0: aa\n
        // 1:│\n
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretIndex());

        sb.prev();
        // 0: aa│\n
        // 1:\n
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretIndex());

        sb.delete(1);
        sb.next();
        // 0: aa\n
        // 1:│
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretIndex());

        sb.add("b");sb.add("b");sb.add("b");
        // 0: aa\n
        // 1: bbb│
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(6, sb.getCaretIndex());

        sb.home();
        // 0: aa\n
        // 1:│bbb
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(3, sb.getCaretIndex());

        sb.end();
        // 0: aa\n
        // 1: bbb│
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(6, sb.getCaretIndex());

        sb.prev();sb.add("\n");
        // 0: aa\n
        // 1: bb\n
        // 2: │b
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(6, sb.getCaretIndex());

        sb.add("c");sb.add("c");sb.add("\n");sb.delete(1);
        // 0: aa\n
        // 1: bb\n
        // 2: cc\n
        // 3:│
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(9, sb.getCaretIndex());
        assertEquals("""
            aa
            bb
            cc
            """, sb.peekString(0, 9));

        sb.prev(); sb.prev();
        // 0: aa\n
        // 1: bb\n
        // 2: c│c\n
        // 3:
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals(7, sb.getCaretIndex());

        sb.next(); sb.prevLine();
        // 0: aa\n
        // 1: bb│\n
        // 2: cc\n
        // 3:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(5, sb.getCaretIndex());

        sb.prevLine(); sb.delete(1);
        // 0: aa│bb\n
        // 1: cc\n
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.getCaretIndex());
        assertEquals("""
            aabb
            cc
            """, sb.peekString(0, 8));

        sb.backSpace(); sb.backSpace(); sb.backSpace();
        // 0:│bb\n
        // 1: cc\n
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.next(); sb.nextLine();
        // 0: bb\n
        // 1: c│c\n
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals(4, sb.getCaretIndex());

        sb.delete(1); sb.delete(1);
        // 0: bb\n
        // 1: c│
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals(4, sb.getCaretIndex());

        sb.backSpace();
        // 0: bb\n
        // 1: │
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(3, sb.charCountOnScreen());
        assertEquals(3, sb.getCaretIndex());
        assertEquals("""
            bb
            """, sb.peekString(0, 3));

        sb.backSpace(); sb.backSpace(); sb.backSpace();
        // 0:│
        // 1:
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.charCountOnScreen());

    }

    @Test void tests() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa\nb");
        // 0: aa\n
        // 1: b│
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.prev();
        // 0: aa\n
        // 1:│b
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("\n");
        // 0: aa\n
        // 1: \n
        // 2:│b
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.prev();
        // 0: aa\n
        // 1:│\n
        // 2: b
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("b");
        // 0: aa\n
        // 1: b│\n
        // 2: b
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.add("b");
        // 0: aa\n
        // 1: bb│\n
        // 2: b
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());

    }
}
