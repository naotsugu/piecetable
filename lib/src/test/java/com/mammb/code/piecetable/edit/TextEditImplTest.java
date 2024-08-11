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
import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link TextEditImpl}.
 * @author Naotsugu Kobayashi
 */
class TextEditImplTest {

    @Test
    void testInsert() {
        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc");

        var e = (Ins) te.getDeque().peek();
        assertEquals(new Pos(0, 0), e.from());
        assertEquals(new Pos(0, 3), e.to());
        assertEquals("abc", e.text());
    }

    @Test
    void testInsertMultiRow() {
        var te = new TextEditImpl(Document.of());
        te.insert(0, 0, "abc\ndef\ngh");

        var e = (Ins) te.getUndo().peek().flip();
        assertEquals(new Pos(0, 0), e.from());
        assertEquals(new Pos(2, 2), e.to());
        assertEquals("abc\ndef\ngh", e.text());
    }

    @Test
    void testDelete() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc");

        te.delete(0, 1, 1);

        var e = (Del) te.getDeque().peek();
        assertEquals(new Pos(0, 1), e.from());
        assertEquals(new Pos(0, 1), e.to());
        assertEquals("b", e.text());
    }

    @Test
    void testDeleteMultiRow() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc\ndef\ngh");

        te.delete(0, 1, 8);

        var e = (Del) te.getUndo().peek().flip();
        assertEquals(new Pos(0, 1), e.from());
        assertEquals(new Pos(0, 1), e.to());
        assertEquals("bc\ndef\ng", e.text());
    }

    @Test
    void testBackspace() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc");

        te.backspace(0, 2, 1);

        var e = (Del) te.getDeque().peek();
        assertEquals(new Pos(0, 2), e.from());
        assertEquals(new Pos(0, 1), e.to());
        assertEquals("b", e.text());
    }

    @Test
    void testBackspaceMultiRow() {
        var te = new TextEditImpl(Document.of());
        te.getDoc().insert(0, 0, "abc\ndef\ngh");

        te.backspace(2, 1, 8);

        var e = (Del) te.getUndo().peek().flip();
        assertEquals(new Pos(2, 1), e.from());
        assertEquals(new Pos(0, 1), e.to());
        assertEquals("bc\ndef\ng", e.text());
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

}
