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
 * The test of {@link Texts}.
 * @author Naotsugu Kobayashi
 */
class TextsTest {

    @Test
    void splitRowBreak() {
        var ret = Texts.splitRowBreak("");
        assertEquals(1, ret.size());
        assertEquals("", ret.get(0));

        ret = Texts.splitRowBreak("a");
        assertEquals(1, ret.size());
        assertEquals("a", ret.get(0));

        ret = Texts.splitRowBreak("a\n");
        assertEquals(2, ret.size());
        assertEquals("a\n", ret.get(0));
        assertEquals("", ret.get(1));

        ret = Texts.splitRowBreak("a\nb");
        assertEquals(2, ret.size());
        assertEquals("a\n", ret.get(0));
        assertEquals("b", ret.get(1));

        ret = Texts.splitRowBreak("ab\ncd\nef");
        assertEquals(3, ret.size());
        assertEquals("ab\n", ret.get(0));
        assertEquals("cd\n", ret.get(1));
        assertEquals("ef", ret.get(2));
    }

    @Test
    void chLength() {
        assertEquals(0, Texts.chLength(""));
        assertEquals(1, Texts.chLength("a"));
        assertEquals(2, Texts.chLength("ab"));
        assertEquals(3, Texts.chLength("ab\n"));
        assertEquals(5, Texts.chLength("ab\ncd"));
        assertEquals(5, Texts.chLength("a𠀋\r\nbc"));
        assertEquals(8, Texts.chLength("a𠀋\r\nbc\r\n12"));
    }

    @Test
    void chLeft() {
        assertEquals("", Texts.chLeft("", 0));
        assertEquals("", Texts.chLeft("a", 0));
        assertEquals("a", Texts.chLeft("a", 1));
        assertEquals("a", Texts.chLeft("ab", 1));
        assertEquals("a", Texts.chLeft("abc", 1));
        assertEquals("ab", Texts.chLeft("abc", 2));
        assertEquals("abc", Texts.chLeft("abc", 3));
        assertEquals("abc", Texts.chLeft("abc", 4));

        assertEquals("ab", Texts.chLeft("ab\n", 2));
        assertEquals("ab\n", Texts.chLeft("ab\n", 3));
        assertEquals("ab\n", Texts.chLeft("ab\n", 4));

        assertEquals("ab", Texts.chLeft("ab\r\n", 2));
        assertEquals("ab\r\n", Texts.chLeft("ab\r\n", 3));
        assertEquals("ab\r\n", Texts.chLeft("ab\r\n", 4));
        assertEquals("ab\r\n", Texts.chLeft("ab\r\n", 5));

        assertEquals("a𠀋", Texts.chLeft("a𠀋b\r\n", 2));
        assertEquals("a𠀋b", Texts.chLeft("a𠀋b\r\n", 3));
        assertEquals("a𠀋b\r\n", Texts.chLeft("a𠀋b\r\n", 4));
        assertEquals("a𠀋b\r\n", Texts.chLeft("a𠀋b\r\n", 5));

    }

    @Test
    void chRight() {
        assertEquals("", Texts.chRight("", 0));
        assertEquals("", Texts.chRight("a", 0));
        assertEquals("a", Texts.chRight("a", 1));
        assertEquals("b", Texts.chRight("ab", 1));
        assertEquals("c", Texts.chRight("abc", 1));
        assertEquals("bc", Texts.chRight("abc", 2));
        assertEquals("abc", Texts.chRight("abc", 3));
        assertEquals("abc", Texts.chRight("abc", 4));

        assertEquals("b\n", Texts.chRight("ab\n", 2));
        assertEquals("ab\n", Texts.chRight("ab\n", 3));
        assertEquals("ab\n", Texts.chRight("ab\n", 4));

        assertEquals("b\r\n", Texts.chRight("ab\r\n", 2));
        assertEquals("ab\r\n", Texts.chRight("ab\r\n", 3));
        assertEquals("ab\r\n", Texts.chRight("ab\r\n", 4));
        assertEquals("ab\r\n", Texts.chRight("ab\r\n", 5));

        assertEquals("b\r\n", Texts.chRight("a𠀋b\r\n", 2));
        assertEquals("𠀋b\r\n", Texts.chRight("a𠀋b\r\n", 3));
        assertEquals("a𠀋b\r\n", Texts.chRight("a𠀋b\r\n", 4));
        assertEquals("a𠀋b\r\n", Texts.chRight("a𠀋b\r\n", 5));

    }


}
