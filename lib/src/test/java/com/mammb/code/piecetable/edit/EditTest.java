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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link Edit}.
 * @author Naotsugu Kobayashi
 */
class EditTest {

    @Test
    void mergeInsert() {
        var e = (Edit.Ins) Edit.insert(0, 0, "a").merge(Edit.insert(0, 1, "b")).get();
        assertEquals(e.range().row(), 0);
        assertEquals(e.range().col(), 0);
        assertTrue(e.range().left());
        assertEquals(e.text(), "ab");
    }

    @Test
    void mergeDelete() {
        var e = (Edit.Del) Edit.delete(0, 0, "a").merge(Edit.delete(0, 0, "b")).get();
        assertEquals(e.range().row(), 0);
        assertEquals(e.range().col(), 0);
        assertTrue(e.range().right());
        assertEquals(e.text(), "ab");
    }

    @Test
    void mergeBackspace() {
        var e = (Edit.Del) Edit.backspace(0, 2, "b").merge(Edit.backspace(0, 1, "a")).get();
        assertEquals(e.range().row(), 0);
        assertEquals(e.range().col(), 2);
        assertTrue(e.range().left());
        assertEquals(e.text(), "ab");
    }

    @Test
    void flipInsert() {
        var e = (Edit.Del) Edit.insert(0, 0, "a").flip();
        assertEquals(e.range().row(), 0);
        assertEquals(e.range().col(), 0);
        assertTrue(e.range().left());
        assertEquals(e.text(), "a");
    }

    @Test
    void flipDelete() {
        var e = (Edit.Ins) Edit.delete(0, 0, "a").flip();
        assertEquals(e.range().row(), 0);
        assertEquals(e.range().col(), 0);
        assertTrue(e.range().right());
        assertEquals(e.text(), "a");
    }

    @Test
    void flipBackspace() {
        var e = (Edit.Ins) Edit.backspace(0, 1, "a").flip();
        assertEquals(e.range().row(), 0);
        assertEquals(e.range().col(), 1);
        assertTrue(e.range().left());
        assertEquals(e.text(), "a");
    }

}
