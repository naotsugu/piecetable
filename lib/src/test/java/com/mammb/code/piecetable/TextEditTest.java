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
package com.mammb.code.piecetable;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link TextEdit}.
 * @author Naotsugu Kobayashi
 */
class TextEditTest {

    @Test
    void testInsert() {

        var te = TextEdit.of();
        te.insert(0, 0, "a");
        te.insert(0, 1, "b");
        te.insert(0, 2, "c");
        assertEquals("abc", te.getText(0));

        te.undo();
        assertEquals("", te.getText(0));

        te.redo();
        assertEquals("abc", te.getText(0));
    }

    @Test
    void testDelete() {

        var te = TextEdit.of();
        te.insert(0, 0, "abc");te.flush();
        assertEquals(1, te.rows());
        assertEquals("abc", te.getText(0));

        te.delete(0, 0, 1);
        assertEquals("bc", te.getText(0));

        te.delete(0, 0, 1);
        assertEquals("c", te.getText(0));

        te.delete(0, 0, 1);
        assertEquals("", te.getText(0));

        te.undo();
        assertEquals("abc", te.getText(0));

        te.redo();
        assertEquals("", te.getText(0));
    }


//    @Test
//    void testBackspace() {
//
//        var te = TextEdit.of();
//        te.insert(0, 0, "abc");
//        assertEquals("abc", te.getText(0));
//
//        te.backspace(0, 3, 1);
//        assertEquals("ab", te.getText(0));
//
//        te.backspace(0, 2, 1);
//        assertEquals("a", te.getText(0));
//
//        te.backspace(0, 1, 1);
//        assertEquals("", te.getText(0));
//
//        te.undo();
//        assertEquals("abc", te.getText(0));
//
//        te.redo();
//        assertEquals("", te.getText(0));
//
//    }

    @Test
    void testPos() {
        var pos1 = new TextEdit.Pos(0, 0);
        var pos2 = new TextEdit.Pos(0, 2);
        var pos3 = new TextEdit.Pos(0, 2);
        var pos4 = new TextEdit.Pos(1, 2);
        var pos5 = new TextEdit.Pos(1, 4);
        var pos6 = new TextEdit.Pos(4, 4);
        var pos7 = new TextEdit.Pos(4, 5);

        var list1 = List.of(pos4, pos2, pos7, pos1, pos3, pos6, pos5)
            .stream().sorted().toList();

        assertEquals(pos1, list1.get(0));
        assertEquals(pos2, list1.get(1));
        assertEquals(pos3, list1.get(2));
        assertEquals(pos4, list1.get(3));
        assertEquals(pos5, list1.get(4));
        assertEquals(pos6, list1.get(5));
        assertEquals(pos7, list1.get(6));

        list1 = list1.stream().sorted(Comparator.reverseOrder()).toList();

        assertEquals(pos7, list1.get(0));
        assertEquals(pos6, list1.get(1));
        assertEquals(pos5, list1.get(2));
        assertEquals(pos4, list1.get(3));
        assertEquals(pos3, list1.get(4));
        assertEquals(pos2, list1.get(5));
        assertEquals(pos1, list1.get(6));

    }

    @Test
    void test() {

        var edit = TextEdit.of();

        edit.insert(0, 0, "a large text");
        edit.insert(0, 8, "span of ");
        edit.delete(0, 1, 6);
        assertEquals("a span of text", edit.getText(0));

        edit.undo();
        assertEquals("a large span of text", edit.getText(0));

        edit.redo();
        assertEquals("a span of text", edit.getText(0));

    }
}
