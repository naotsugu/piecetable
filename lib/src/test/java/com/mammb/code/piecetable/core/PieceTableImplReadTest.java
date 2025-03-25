/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.core;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link PieceTableImpl#read(long, long, ByteBuffer)}.
 * @author Naotsugu Kobayashi
 */
public class PieceTableImplReadTest {

    @Test
    void readSmallByteBuffer() {
        var pt = PieceTableImpl.of();
        pt.insert(0, "ab".getBytes());
        pt.insert(2, "cd".getBytes());
        pt.insert(4, "ef".getBytes());

        var bb = ByteBuffer.allocate(5);
        long ret = pt.read(0, 6, bb);

        assertEquals(5, ret);
        assertEquals("abcde", new String(bb.array()));
    }

    @Test
    void read() {
        var pt = PieceTableImpl.of();
        pt.insert(0, "ab".getBytes());
        pt.insert(2, "cd".getBytes());
        pt.insert(4, "ef".getBytes());

        var bb = ByteBuffer.allocate(10);
        long ret = pt.read(0, 6, bb);
        bb.flip();
        assertEquals(6, ret);
        assertEquals("abcdef", StandardCharsets.UTF_8.decode(bb).toString());
    }

    @Test
    void readRange() {
        var pt = PieceTableImpl.of();
        pt.insert(0, "ab".getBytes());
        pt.insert(2, "cd".getBytes());
        pt.insert(4, "ef".getBytes());

        var bb = ByteBuffer.allocate(10);
        long ret = pt.read(1, 4, bb);
        bb.flip();
        assertEquals(4, ret);
        assertEquals("bcde", StandardCharsets.UTF_8.decode(bb).toString());
    }

}
