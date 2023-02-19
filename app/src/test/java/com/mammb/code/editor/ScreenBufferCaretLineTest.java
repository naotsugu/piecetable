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

public class ScreenBufferCaretLineTest {

    @Test
    void test() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(10);
        sb.add("\n");
        sb.add("aaaaaaaaaa\n");
        sb.add("bbb\n");
        sb.add("cccccc\n");
        assertEquals(4, sb.getCaretOffsetY());

        sb.prevLine();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3:|cccccc\n
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(16, sb.getCaretOffset());

        sb.end();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3: cccccc|\n
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(6, sb.caretLineCharOffset());
        assertEquals(22, sb.getCaretOffset());

        sb.prevLine();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb|\n
        // 3: cccccc\n
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(3, sb.caretLineCharOffset());
        assertEquals(15, sb.getCaretOffset());

        sb.prevLine();
        // 0: \n
        // 1: aaaaaa|aaaa\n
        // 2: bbb\n
        // 3: cccccc\n
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(6, sb.caretLineCharOffset());
        assertEquals(7, sb.getCaretOffset());

        sb.prevLine();
        // 0:|\n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3: cccccc\n
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(0, sb.getCaretOffset());

        sb.prevLine();
        // 0:|\n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3: cccccc\n
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(0, sb.getCaretOffset());

        sb.nextLine();
        // 0: \n
        // 1: aaaaaa|aaaa\n
        // 2: bbb\n
        // 3: cccccc\n
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(6, sb.caretLineCharOffset());
        assertEquals(7, sb.getCaretOffset());

        sb.nextLine();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb|\n
        // 3: cccccc\n
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(3, sb.caretLineCharOffset());
        assertEquals(15, sb.getCaretOffset());

        sb.nextLine();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3: cccccc|\n
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(6, sb.caretLineCharOffset());
        assertEquals(22, sb.getCaretOffset());

        sb.nextLine();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3: cccccc\n
        // 4:|
        assertEquals(4, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(23, sb.getCaretOffset());

        sb.nextLine();
        // 0: \n
        // 1: aaaaaaaaaa\n
        // 2: bbb\n
        // 3: cccccc\n
        // 4:|
        assertEquals(4, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(23, sb.getCaretOffset());
    }

    @Test void test2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("ccc\n");
        // 0: ccc\n
        // 1:|
        sb.prevLine();
        sb.prevLine();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretOffset());

        sb.add("aaa\nbbb");
        // 0: aab\n
        // 1: bbb|ccc\n
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(7, sb.getCaretOffset());

        sb.nextLine();
        // 0: aab\n
        // 1: bbbccc\n
        // 2:|
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(11, sb.getCaretOffset());

        sb.nextLine();
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals(0, sb.caretLineCharOffset());
        assertEquals(11, sb.getCaretOffset());
    }

}
