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
package com.mammb.code.editor3.ui;

import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link TextFlow}.
 * @author Naotsugu Kobayashi
 */
class TextFlowTest {


    @Test
    void testRowOffset() {
        var textFlow = new TextFlow();
        textFlow.setAll(List.of(new Text("ab\n"), new Text("cde\n"), new Text("fgh\n")));

        var ret = textFlow.rowOffset(0);
        assertEquals(0, ret[0]);
        assertEquals(2, ret[1]);

        ret = textFlow.rowOffset(1);
        assertEquals(3, ret[0]);
        assertEquals(6, ret[1]);

        ret = textFlow.rowOffset(2);
        assertEquals(7, ret[0]);
        assertEquals(10, ret[1]);

    }

    @Test
    void testCharAt() {
        var textFlow = new TextFlow();
        textFlow.setAll(List.of(new Text("ab"), new Text("cde"), new Text("fgh")));
        assertEquals('a', textFlow.charAt(0));
        assertEquals('b', textFlow.charAt(1));
        assertEquals('c', textFlow.charAt(2));
        assertEquals('d', textFlow.charAt(3));
        assertEquals('e', textFlow.charAt(4));
        assertEquals('f', textFlow.charAt(5));
        assertEquals('g', textFlow.charAt(6));
        assertEquals('h', textFlow.charAt(7));
    }

    @Test
    void testSubSequence() {
        var textFlow = new TextFlow();
        textFlow.setAll(List.of(new Text("ab"), new Text("cde"), new Text("fgh")));

        assertEquals("a", textFlow.subSequence(0, 1).toString());
        assertEquals("ab", textFlow.subSequence(0, 2).toString());
        assertEquals("abc", textFlow.subSequence(0, 3).toString());
        assertEquals("abcd", textFlow.subSequence(0, 4).toString());
        assertEquals("abcde", textFlow.subSequence(0, 5).toString());
        assertEquals("abcdef", textFlow.subSequence(0, 6).toString());

        assertEquals("b", textFlow.subSequence(1, 2).toString());
        assertEquals("bc", textFlow.subSequence(1, 3).toString());
        assertEquals("bcd", textFlow.subSequence(1, 4).toString());
        assertEquals("bcde", textFlow.subSequence(1, 5).toString());
        assertEquals("bcdef", textFlow.subSequence(1, 6).toString());
        assertEquals("bcdefg", textFlow.subSequence(1, 7).toString());

        assertEquals("c", textFlow.subSequence(2, 3).toString());
        assertEquals("cd", textFlow.subSequence(2, 4).toString());
        assertEquals("cde", textFlow.subSequence(2, 5).toString());
        assertEquals("cdef", textFlow.subSequence(2, 6).toString());
        assertEquals("cdefg", textFlow.subSequence(2, 7).toString());
        assertEquals("cdefgh", textFlow.subSequence(2, 8).toString());

        assertEquals("d", textFlow.subSequence(3, 4).toString());
        assertEquals("de", textFlow.subSequence(3, 5).toString());
        assertEquals("def", textFlow.subSequence(3, 6).toString());
        assertEquals("defg", textFlow.subSequence(3, 7).toString());
        assertEquals("defgh", textFlow.subSequence(3, 8).toString());

        assertEquals("e", textFlow.subSequence(4, 5).toString());
        assertEquals("ef", textFlow.subSequence(4, 6).toString());
        assertEquals("efg", textFlow.subSequence(4, 7).toString());
        assertEquals("efgh", textFlow.subSequence(4, 8).toString());

        assertEquals("f", textFlow.subSequence(5, 6).toString());
        assertEquals("fg", textFlow.subSequence(5, 7).toString());
        assertEquals("fgh", textFlow.subSequence(5, 8).toString());

        assertEquals("g", textFlow.subSequence(6, 7).toString());
        assertEquals("gh", textFlow.subSequence(6, 8).toString());

        assertEquals("h", textFlow.subSequence(7, 8).toString());
    }
}
