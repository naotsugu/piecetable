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
package com.mammb.dev.picetable.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link ByteArray}.
 * @author Naotsugu Kobayashi
 */
class ByteArrayTest {
    @Test
    void test() {
        var b = ByteArray.of();
        assertEquals(0, b.length());
        assertEquals(0, b.capacity());

        var val1 = "abc".getBytes();
        b.add(val1);
        assertEquals(3, b.length());
        assertEquals(10, b.capacity());
        assertArrayEquals(val1, b.get());

        var val2 = "123456789".getBytes();
        b.add(val2);
        assertEquals(12, b.length());
        assertEquals(15, b.capacity());

        b.add(val1);
        assertEquals(15, b.length());
        assertEquals(15, b.capacity());

        b.add((byte) 65);
        assertEquals(16, b.length());
        assertEquals(22, b.capacity());
        assertArrayEquals("abc123456789abcA".getBytes(), b.get());

        b.clear();
        assertEquals(0, b.length());
        assertEquals(0, b.capacity());
        assertArrayEquals(new byte[]{}, b.get());

    }

}
