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

import com.mammb.code.piecetable.Document;
import org.junit.jupiter.api.Test;
import com.mammb.code.piecetable.edit.Edit.*;
import com.mammb.code.piecetable.TextEdit.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link TextEditImpl}.
 * @author Naotsugu Kobayashi
 */
class TextEditImplTest {

    @Test
    void testInsert() {
        var te = new TextEditImpl(Document.of());

        var pos = te.insert(0, 0, "abc");
        assertEquals("abc", te.getText(0, 1));
        assertEquals(new Pos(0, 3), pos);
    }

    @Test
    void testInsertMultiRow() {
        var te = new TextEditImpl(Document.of());

        var pos = te.insert(0, 0, "abc\ndef\ngh");

        assertEquals("abc\ndef\ngh", te.getText(0, 3));
        assertEquals(new Pos(2, 2), pos);
    }

    @Test
    void testInsertMultiPos() {
        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123\ndef");

        // | a | b | c | 1 | 2 | 3 | $ | d | e | f |
        // |           |                   |
        // ------------------------------------------
        // | * | * | a | b | c | * | * | 1 | 2 | 3 | $ | d | * | * | e | f |
        //         |                   |                           |
        var posList = te.insert(List.of(new Pos(0, 0), new Pos(0, 3), new Pos(1, 1)), "**");
        assertEquals("**abc**123\nd**ef", te.getText(0, 2));
        assertEquals(new Pos(0, 2), posList.get(0));
        assertEquals(new Pos(0, 7), posList.get(1));
        assertEquals(new Pos(1, 3), posList.get(2));
    }

    @Test
    void testInsertMultiPosMultiLine() {
        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123\ndef");

        // | a | b | c | 1 | 2 | 3 | $ | d | e | f |
        // |           |                   |
        // ------------------------------------------
        // | * | $
        // | * | a | b | c | * | $ |
        //     ^
        // | * | 1 | 2 | 3 | $ |
        //     ^
        // | d | * | $
        // | * | e | f |
        //     ^
        var posList = te.insert(List.of(new Pos(0, 0), new Pos(0, 3), new Pos(1, 1)), "*\n*");
        assertEquals("*\n*abc*\n*123\nd*\n*ef", te.getText(0, 5));
        assertEquals(new Pos(1, 1), posList.get(0));
        assertEquals(new Pos(2, 1), posList.get(1));
        assertEquals(new Pos(4, 1), posList.get(2));
    }

    @Test
    void testDelete() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc\r\n");

        var delText = te.delete(0, 3);
        assertEquals("\r\n", delText);
        assertEquals("abc", te.getText(0, 1));
    }

    @Test
    void testDeleteMultiRow() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc\ndef\ngh");

        var delText = te.delete(0, 1, 8);
        assertEquals("bc\ndef\ng", delText);
        assertEquals("ah", te.getText(0, 1));
    }

    @Test
    void testDeleteMulti() {

        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123");

        // | a | b | c | 1 | 2 | 3 |
        // ^***    ^***    ^***
        // ------------------------------------------
        // | b | 1 | 3 |
        // ^   ^   ^
        var posList = te.delete(List.of(new Pos(0, 0), new Pos(0, 2), new Pos(0, 4)));
        assertEquals("b13", te.getText(0, 1));
        assertEquals(new Pos(0, 0), posList.get(0));
        assertEquals(new Pos(0, 1), posList.get(1));
        assertEquals(new Pos(0, 2), posList.get(2));

        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123\ndef");

        // | a | b | c | 1 | 2 | 3 | $ | d | e | f |
        // ^***        ^***                ^***
        // ------------------------------------------
        // | b | c | 2 | 3 | $ | d | f |
        // ^       ^               ^
        posList = te.delete(List.of(new Pos(0, 0), new Pos(0, 3), new Pos(1, 1)));
        assertEquals("bc23\ndf", te.getText(0, 2));
        assertEquals(new Pos(0, 0), posList.get(0));
        assertEquals(new Pos(0, 2), posList.get(1));
        assertEquals(new Pos(1, 1), posList.get(2));

        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123\ndef");

        // | a | b | c | 1 | 2 | 3 | $ | d | e | f |
        // ^***        ^***        ^***        ^***
        // ------------------------------------------
        // | b | c | 2 | 3 | d | e |
        // ^       ^       ^       ^
        posList = te.delete(List.of(new Pos(0, 0), new Pos(0, 3), new Pos(0, 6), new Pos(1, 2)));
        assertEquals("bc23de", te.getText(0, 1));
        assertEquals(new Pos(0, 0), posList.get(0));
        assertEquals(new Pos(0, 2), posList.get(1));
        assertEquals(new Pos(0, 4), posList.get(2));
        assertEquals(new Pos(0, 6), posList.get(3));
    }

    @Test
    void testBackspace() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc");

        var pos = te.backspace(0, 2);
        assertEquals("ac", te.getText(0, 1));
        assertEquals(new Pos(0, 1), pos);
    }

    @Test
    void testBackspaceRowBreak() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc\r\n");

        var pos = te.backspace(1, 0);
        assertEquals("abc", te.getText(0, 1));
        assertEquals(new Pos(0, 3), pos);
    }

    @Test
    void testBackspaceMultiRow() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc\ndef\ngh");

        var pos = te.backspace(2, 1, 8);
        assertEquals("ah", te.getText(0, 1));
        assertEquals(new Pos(0, 1), pos);
    }

    @Test
    void testBackspaceMulti() {

        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123");
        // | a | b | c | 1 | 2 | 3 |
        //  ***^        ***^    ***^
        // ------------------------------------------
        // | b | c | 2 |
        // ^       ^   ^
        var posList = te.backspace(List.of(new Pos(0, 1), new Pos(0, 4), new Pos(0, 6)));
        assertEquals("bc2", te.getText(0, 1));
        assertEquals(new Pos(0, 0), posList.get(0));
        assertEquals(new Pos(0, 2), posList.get(1));
        assertEquals(new Pos(0, 3), posList.get(2));

        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\n123");
        // | a | b | c | $ | 1 | 2 | 3 |
        //      ***^        ***^
        // ------------------------------------------
        // | a | c | $ | 2 | 3 |
        //     ^       ^
        posList = te.backspace(List.of(new Pos(0, 2), new Pos(1, 1)));
        assertEquals("ac\n23", te.getText(0, 2));
        assertEquals(new Pos(0, 1), posList.get(0));
        assertEquals(new Pos(1, 0), posList.get(1));

        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\n123");
        // | a | b | c | $ | 1 | 2 | 3 |
        //  ***^    ***^    ***^    ***^
        // ------------------------------------------
        // | b | $ | 2 |
        // ^   ^   ^   ^
        posList = te.backspace(List.of(new Pos(0, 1), new Pos(0, 3), new Pos(1, 1), new Pos(1, 3)));
        assertEquals("b\n2", te.getText(0, 2));
        assertEquals(new Pos(0, 0), posList.get(0));
        assertEquals(new Pos(0, 1), posList.get(1));
        assertEquals(new Pos(1, 0), posList.get(2));
        assertEquals(new Pos(1, 1), posList.get(3));

        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\n123");
        // | a | b | c | $ | 1 | 2 | 3 |
        //     ***^     ***^    ***^
        // ------------------------------------------
        // | a | c | 1 | 3 |
        //     ^   ^   ^
        posList = te.backspace(List.of(new Pos(0, 2), new Pos(1, 0), new Pos(1, 2)));
        assertEquals("ac13", te.getText(0, 1));
        assertEquals(new Pos(0, 1), posList.get(0));
        assertEquals(new Pos(0, 2), posList.get(1));
        assertEquals(new Pos(0, 3), posList.get(2));
    }

    @Test
    void testBackspaceMulti2() {
        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc123\ndef");

        // | a | b | c | 1 | 2 | 3 | $ | d | e | f |
        //     |           |           |           |
        // ------------------------------------------
        // | b | c | 2 | 3 | d | e |
        // |       |       |       |
        var posList = te.backspace(List.of(new Pos(0, 1), new Pos(0, 4), new Pos(1, 0), new Pos(1, 3)));
        assertEquals("bc23de", te.getText(0, 1));
        assertEquals(new Pos(0, 0), posList.get(0));
        assertEquals(new Pos(0, 2), posList.get(1));
        assertEquals(new Pos(0, 4), posList.get(2));
        assertEquals(new Pos(0, 6), posList.get(3));
    }

    @Test
    void testDryApplyInsert() {

        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc");

        var e = new Ins(new Pos(0, 0), new Pos(0, 1), "1");
        te.testDryApply(e);
        assertEquals("1abc", te.getDryBuffer().get(0));
        assertEquals("1abc", te.getText(0));
        assertEquals("abc", te.getDoc().getText(0));

        e = new Ins(new Pos(0, 0), new Pos(0, 1), "2");
        te.testDryApply(e);
        assertEquals("21abc", te.getDryBuffer().get(0));
        assertEquals("21abc", te.getText(0));
        assertEquals("abc", te.getDoc().getText(0));

    }

    @Test
    void testDryApplyDelete() {

        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc");

        var e = new Del(new Pos(0, 0), new Pos(0, 0), "a");
        te.testDryApply(e);
        assertEquals("bc", te.getDryBuffer().get(0));
        assertEquals("bc", te.getText(0));
        assertEquals("abc", te.getDoc().getText(0));

        e = new Del(new Pos(0, 0), new Pos(0, 0), "b");
        te.testDryApply(e);
        assertEquals("c", te.getDryBuffer().get(0));
        assertEquals("c", te.getText(0));
        assertEquals("abc", te.getDoc().getText(0));

    }

    @Test
    void testDistances() {
        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\ndef\nghi");

        // | a | b | c | $ | d | e | f | $ | g | h | i |
        // |   |               |   |                   |
        // (0, 0)              (1, 1)                  (2, 3)
        //     (0, 1)              (1, 2)
        // 0   1               5   6                   11
        var p1 = new Pos(0, 0);
        var p2 = new Pos(0, 1);
        var p3 = new Pos(1, 1);
        var p4 = new Pos(1, 2);
        var p5 = new Pos(2, 3);

        int[] distances = te.distances(List.of(p1, p2, p3, p4, p5));

        assertEquals(0, distances[0]);
        assertEquals(1, distances[1]);
        assertEquals(5, distances[2]);
        assertEquals(6, distances[3]);
        assertEquals(11, distances[4]);

        List<Pos> posList = te.posList(0, distances);

        assertEquals(p1, posList.get(0));
        assertEquals(p2, posList.get(1));
        assertEquals(p3, posList.get(2));
        assertEquals(p4, posList.get(3));
        assertEquals(p5, posList.get(4));

    }

    @Test
    void testTextRight() {

        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\ndef\nghi");

        assertEquals("a", te.textRight(0, 0, 1).get(0));
        assertEquals("b", te.textRight(0, 1, 1).get(0));
        assertEquals("bc\n", te.textRight(0, 1, 3).get(0));
        assertEquals("", te.textRight(0, 1, 3).get(1));

        var ret = te.textRight(0, 1, 4);
        assertEquals(2, ret.size());
        assertEquals("bc\n", ret.get(0));
        assertEquals("d", ret.get(1));

        ret = te.textRight(0, 1, 8);
        assertEquals(3, ret.size());
        assertEquals("bc\n", ret.get(0));
        assertEquals("def\n", ret.get(1));
        assertEquals("g", ret.get(2));


        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "a𠀋c\r\nd𠀋f\r\ng𠀋i");

        ret = te.textRight(0, 1, 4);
        assertEquals(2, ret.size());
        assertEquals("𠀋c\r\n", ret.get(0));
        assertEquals("d", ret.get(1));

        ret = te.textRight(0, 1, 8);
        assertEquals(3, ret.size());
        assertEquals("𠀋c\r\n", ret.get(0));
        assertEquals("d𠀋f\r\n", ret.get(1));
        assertEquals("g", ret.get(2));

        ret = te.textRight(0, 4, 8);
        assertEquals(3, ret.size());
        assertEquals("\r\n", ret.get(0));
        assertEquals("d𠀋f\r\n", ret.get(1));
        assertEquals("g𠀋i", ret.get(2));
    }

    @Test
    void testTextLeft() {

        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\ndef\nghi");

        assertEquals("i", te.textLeft(2, 3, 1).get(0));
        assertEquals("h", te.textLeft(2, 2, 1).get(0));
        assertEquals("g", te.textLeft(2, 1, 1).get(0));
        assertEquals("\n", te.textLeft(2, 0, 1).get(0));
        assertEquals("\n", te.textLeft(2, 2, 3).get(0));
        assertEquals("gh", te.textLeft(2, 2, 3).get(1));

        var ret = te.textLeft(2, 1, 4);
        assertEquals(2, ret.size());
        assertEquals("ef\n", ret.get(0));
        assertEquals("g", ret.get(1));

        ret = te.textLeft(2, 3, 9);
        assertEquals(3, ret.size());
        assertEquals("c\n", ret.get(0));
        assertEquals("def\n", ret.get(1));
        assertEquals("ghi", ret.get(2));


        te = new TextEditImpl(Document.of());
        te.insert(0, 0, "a𠀋c\r\nd𠀋f\r\ng𠀋i");

        ret = te.textLeft(2, 3, 4);
        assertEquals(2, ret.size());
        assertEquals("f\r\n", ret.get(0));
        assertEquals("g𠀋", ret.get(1));

        ret = te.textLeft(2, 4, 10);
        assertEquals(3, ret.size());
        assertEquals("𠀋c\r\n", ret.get(0));
        assertEquals("d𠀋f\r\n", ret.get(1));
        assertEquals("g𠀋i", ret.get(2));

        ret = te.textLeft(1, 4, 7);
        assertEquals(2, ret.size());
        assertEquals("a𠀋c\r\n", ret.get(0));
        assertEquals("d𠀋f", ret.get(1));
    }

    @Test
    void testTextRightByte() {

        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\ndef\nghi");

        assertEquals("a", te.textRightByte(0, 0, 1).get(0));
        assertEquals("b", te.textRightByte(0, 1, 1).get(0));
        assertEquals("bc\n", te.textRightByte(0, 1, 3).get(0));

        var ret = te.textRightByte(0, 1, 4);
        assertEquals(2, ret.size());
        assertEquals("bc\n", ret.get(0));
        assertEquals("d", ret.get(1));

        ret = te.textRightByte(0, 1, 8);
        assertEquals(3, ret.size());
        assertEquals("bc\n", ret.get(0));
        assertEquals("def\n", ret.get(1));
        assertEquals("g", ret.get(2));
    }

    @Test
    void testGetText() {

        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\ndef\nghi");

        assertEquals("", te.getText(0, 0, 0, 0));
        assertEquals("a", te.getText(0, 0, 0, 1));
        assertEquals("ab", te.getText(0, 0, 0, 2));
        assertEquals("abc", te.getText(0, 0, 0, 3));
        assertEquals("abc\n", te.getText(0, 0, 0, 4));

        assertEquals("abc\n", te.getText(0, 4, 0, 0));

        assertEquals("f\ngh", te.getText(1, 2, 2, 2));
        assertEquals("abc\ndef\nghi", te.getText(0, 0, 2, 3));
        assertEquals("abc\ndef\nghi", te.getText(2, 3, 0, 0));

    }

}
