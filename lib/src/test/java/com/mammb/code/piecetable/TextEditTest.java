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
        te.insert(0, 0, "abc");
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


    @Test
    void testBackspace() {

        var te = TextEdit.of();
        te.insert(0, 0, "abc");
        assertEquals("abc", te.getText(0));

        te.backspace(0, 3, 1);
        assertEquals("ab", te.getText(0));

        te.backspace(0, 2, 1);
        assertEquals("a", te.getText(0));

        te.backspace(0, 1, 1);
        assertEquals("", te.getText(0));

        te.undo();
        assertEquals("abc", te.getText(0));

        te.redo();
        assertEquals("", te.getText(0));

    }
}
