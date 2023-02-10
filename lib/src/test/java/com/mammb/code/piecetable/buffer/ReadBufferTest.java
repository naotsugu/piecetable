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
package com.mammb.code.piecetable.buffer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link ReadBuffer}.
 * @author Naotsugu Kobayashi
 */
class ReadBufferTest {

    @Test
    void testReadBuffer() {

        var b = ReadBuffer.of("01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8), (short) 10);

        assertEquals(16, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));
        assertEquals("②", new String(b.charAt(2), StandardCharsets.UTF_8));
        assertEquals("Ｆ", new String(b.charAt(15), StandardCharsets.UTF_8));

        assertArrayEquals("01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8), b.bytes(0, 20));
        assertEquals("1②34", b.subBuffer(1, 5).toString());

    }
}
