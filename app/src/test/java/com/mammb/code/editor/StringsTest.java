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

class StringsTest {

    @Test
    void splitLine() {

        var ret = Strings.splitLine("ab");
        assertEquals(1, ret.length);
        assertEquals("ab", ret[0]);

        ret = Strings.splitLine("");
        assertEquals(1, ret.length);
        assertEquals("", ret[0]);

        ret = Strings.splitLine("ab\ncd");
        assertEquals(2, ret.length);
        assertEquals("ab\n", ret[0]);
        assertEquals("cd", ret[1]);

        ret = Strings.splitLine("ab\n");
        assertEquals(2, ret.length);
        assertEquals("ab\n", ret[0]);
        assertEquals("", ret[1]);

        ret = Strings.splitLine("\ncd");
        assertEquals(2, ret.length);
        assertEquals("\n", ret[0]);
        assertEquals("cd", ret[1]);

        ret = Strings.splitLine("ab\ncd\nef\n");
        assertEquals(4, ret.length);
        assertEquals("ab\n", ret[0]);
        assertEquals("cd\n", ret[1]);
        assertEquals("ef\n", ret[2]);
        assertEquals("", ret[3]);

        ret = Strings.splitLine("\n");
        assertEquals(2, ret.length);
        assertEquals("\n", ret[0]);
        assertEquals("", ret[1]);
    }
}
