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
 * Test of {@link GrowBuffer}.
 * @author Naotsugu Kobayashi
 */
class GrowBufferTest {

    @Test
    void testGrowBuffer() {

        var b = GrowBuffer.of((short) 10);

        b.append("01");
        assertEquals(2, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));

        assertArrayEquals("01".getBytes(StandardCharsets.UTF_8), b.bytes(0, 2));
        assertEquals("01", b.subBuffer(0, 2).toString());

        b.append("②3456789");
        assertEquals(10, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));
        assertEquals("②", new String(b.charAt(2), StandardCharsets.UTF_8));
        assertEquals("9", new String(b.charAt(9), StandardCharsets.UTF_8));

        assertArrayEquals("②3456789".getBytes(StandardCharsets.UTF_8), b.bytes(2, 12));
        assertEquals("②3456789", b.subBuffer(2, 10).toString());
    }
}
