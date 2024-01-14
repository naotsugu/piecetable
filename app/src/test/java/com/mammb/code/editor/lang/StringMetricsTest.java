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
package com.mammb.code.editor.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link StringMetrics}.
 * @author Naotsugu Kobayashi
 */
class StringMetricsTest {

    @Test
    void testEmpty() {
        var sm = new StringMetrics("");
        sm.init();
        assertEquals(1, sm.rowViewSize());
        assertEquals(0, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
    }


    @Test
    void testSingle() {
        var sm = new StringMetrics("a");
        sm.init();
        assertEquals(1, sm.rowViewSize());
        assertEquals(1, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));

        sm = new StringMetrics("ab");
        sm.init();
        assertEquals(1, sm.rowViewSize());
        assertEquals(2, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));

        sm = new StringMetrics("a😀b");
        sm.init();
        assertEquals(1, sm.rowViewSize());
        assertEquals(3, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
    }

    @Test
    void testMultiEndWithLf() {
        var sm = new StringMetrics("\n");
        sm.init();
        assertEquals(2, sm.rowViewSize());
        assertEquals(1, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
        assertEquals(1, sm.rowOffset(1));

        sm = new StringMetrics("a\n");
        sm.init();
        assertEquals(2, sm.rowViewSize());
        assertEquals(2, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
        assertEquals(2, sm.rowOffset(1));

        sm = new StringMetrics("a😀b\n");
        sm.init();
        assertEquals(2, sm.rowViewSize());
        assertEquals(4, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
        assertEquals(5, sm.rowOffset(1));
    }

    @Test
    void testMulti() {
        var sm = new StringMetrics("\na");
        sm.init();
        assertEquals(2, sm.rowViewSize());
        assertEquals(2, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
        assertEquals(1, sm.rowOffset(1));

        sm = new StringMetrics("a\nb");
        sm.init();
        assertEquals(2, sm.rowViewSize());
        assertEquals(3, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
        assertEquals(2, sm.rowOffset(1));

        sm = new StringMetrics("a😀b\ncd");
        sm.init();
        assertEquals(2, sm.rowViewSize());
        assertEquals(6, sm.codePointCount());
        assertEquals(0, sm.rowOffset(0));
        assertEquals(5, sm.rowOffset(1));
    }

}
