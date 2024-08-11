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

import com.mammb.code.piecetable.TextEdit.Pos;
import com.mammb.code.piecetable.edit.Edit.Del;
import com.mammb.code.piecetable.edit.Edit.Ins;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link Edit}.
 * @author Naotsugu Kobayashi
 */
class EditTest {

    @Test
    void mergeInsert() {
        var e1 = new Ins(new Pos(0, 0), new Pos(0, 1), "a", System.currentTimeMillis());
        var e2 = new Ins(new Pos(0, 1), new Pos(0, 2), "b", System.currentTimeMillis());

        var e = (Ins) e1.merge(e2).get();
        assertEquals(e1.from(), e.from());
        assertEquals(e2.to(), e.to());
        assertEquals("ab", e.text());
    }

    @Test
    void mergeDelete() {
        var e1 = new Del(new Pos(0, 0), new Pos(0, 0), "a", System.currentTimeMillis());
        var e2 = new Del(new Pos(0, 0), new Pos(0, 0), "b", System.currentTimeMillis());

        var e = (Del) e1.merge(e2).get();
        assertEquals(e1.from(), e.from());
        assertEquals(e2.to(), e.to());
        assertEquals("ab", e.text());
    }

    @Test
    void mergeBackspace() {
        var e1 = new Del(new Pos(0, 2), new Pos(0, 1), "b", System.currentTimeMillis());
        var e2 = new Del(new Pos(0, 1), new Pos(0, 0), "a", System.currentTimeMillis());

        var e = (Del) e1.merge(e2).get();
        assertEquals(e1.from(), e.from());
        assertEquals(e2.to(), e.to());
        assertEquals("ab", e.text());
    }

    @Test
    void flipInsert() {
        var e1 = new Ins(new Pos(0, 0), new Pos(0, 1), "a", System.currentTimeMillis());

        var e = (Edit.Del) e1.flip();
        assertEquals(new Pos(0, 1), e.from());
        assertEquals(new Pos(0, 0), e.to());
        assertEquals(e.text(), "a");
    }

    @Test
    void flipDelete() {
        var e1 = new Del(new Pos(0, 0), new Pos(0, 0), "a", System.currentTimeMillis());

        var e = (Edit.Ins) e1.flip();
        assertEquals(new Pos(0, 0), e.from());
        assertEquals(new Pos(0, 0), e.to());
        assertEquals(e.text(), "a");
    }

    @Test
    void flipBackspace() {
        var e1 = new Del(new Pos(0, 1), new Pos(0, 0), "a", System.currentTimeMillis());

        var e = (Edit.Ins) e1.flip();
        assertEquals(new Pos(0, 0), e.from());
        assertEquals(new Pos(0, 1), e.to());
        assertEquals(e.text(), "a");
    }

}
