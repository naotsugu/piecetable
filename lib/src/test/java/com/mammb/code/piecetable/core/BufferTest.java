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
package com.mammb.code.piecetable.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link Buffer}.
 * @author Naotsugu Kobayashi
 */
class BufferTest {

    @Test
    void test() {
        var b = Buffer.of(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(5, b.length());
        assertEquals(0, b.get(0));
        assertEquals(4, b.get(4));
        assertArrayEquals(new byte[] { 1, 2, 3 }, b.bytes(1, 4));
    }

    @Test
    void read() {

        var b = Buffer.of(new byte[] { 0, 1, 2, 3, 4 });
        var bb = ByteBuffer.allocate(2);

        assertEquals(-1, b.read(0, 1, bb));
        assertEquals(0, bb.flip().get());
        bb.clear();

        assertEquals(-1, b.read(0, 2, bb));
        assertEquals(0, bb.flip().get());
        assertEquals(1, bb.get());
        bb.clear();

        assertEquals(2, b.read(0, 5, bb));
        assertEquals(0, bb.flip().get());
        assertEquals(1, bb.get());
        bb.compact();
        assertEquals(4, b.read(2, 3, bb));
        assertEquals(2, bb.flip().get());
        assertEquals(3, bb.get());
        bb.compact();
        assertEquals(-1, b.read(4, 1, bb));
        assertEquals(4, bb.flip().get());
        bb.compact();

    }

    @Test
    void write(@TempDir Path tempDir) throws Exception {

        var path = tempDir.resolve("test_write.txt");
        try (FileChannel ch = FileChannel.open(path,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            var b = Buffer.of("01234".getBytes());
            var bb = ByteBuffer.allocate(2);
            assertEquals(3, b.write(ch, bb, 1, 3));
        }
        assertEquals("123", Files.readString(path));
    }

}
