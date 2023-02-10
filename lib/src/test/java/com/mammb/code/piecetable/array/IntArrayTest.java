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
package com.mammb.code.piecetable.array;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link IntArray}.
 * @author Naotsugu Kobayashi
 */
class IntArrayTest {

    @Test
    void test() {

        var i = IntArray.of();
        assertEquals(0, i.length());
        assertEquals(0, i.capacity());

        i.add(new int[] { 1,2,3 });
        assertEquals(3, i.length());
        assertEquals(10, i.capacity());
        assertArrayEquals(new int[] { 1,2,3 }, i.get());

        i.add(new int[] { 1,2,3,4,5,6,7,8,9 });
        assertEquals(12, i.length());
        assertEquals(15, i.capacity());

        i.add(new int[] { 1,2,3 });
        assertEquals(15, i.length());
        assertEquals(15, i.capacity());

        i.add(99);
        assertEquals(16, i.length());
        assertEquals(22, i.capacity());
        assertArrayEquals(new int[] { 1,2,3,1,2,3,4,5,6,7,8,9,1,2,3,99 }, i.get());

        i.clear();
        assertEquals(0, i.length());
        assertEquals(0, i.capacity());
        assertArrayEquals(new int[]{}, i.get());

    }
}
