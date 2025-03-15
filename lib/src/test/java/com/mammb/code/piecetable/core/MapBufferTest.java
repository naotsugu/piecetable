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
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link MapBuffer}.
 * @author Naotsugu Kobayashi
 */
class MapBufferTest {

    @Test
    void get(@TempDir Path tempDir) throws Exception {

        var path = tempDir.resolve("test_get.txt");
        Files.write(path, List.of("a", "b"));

        try (var cb = MapBuffer.of(path)) {
            assertEquals(4, cb.length());
            assertEquals('a', cb.get(0));
            assertEquals('\n', cb.get(1));
            assertEquals('b', cb.get(2));
            assertEquals('\n', cb.get(3));
        }
    }

    @Test
    void bytes(@TempDir Path tempDir) throws Exception {

        var path = tempDir.resolve("test_bytes.txt");
        Files.write(path, List.of(
            "a".repeat(100), "b"));

        try (var cb = MapBuffer.of(path)) {
            long i = 100;
            assertEquals(i + 3, cb.length());
            assertEquals('a', cb.bytes(i - 1, i)[0]);
            assertEquals('\n', cb.bytes(i, i + 1)[0]);
            assertEquals('b', cb.bytes(i + 1, i + 2)[0]);
            assertArrayEquals("a\nb".getBytes(), cb.bytes(i - 1, i + 2));
        }
    }

    @Test
    void read(@TempDir Path tempDir) throws Exception {

        var path = tempDir.resolve("test_read.txt");
        Files.write(path, List.of("0123"));
        var bb = ByteBuffer.allocate(2);

        try (var cb = MapBuffer.of(path)) {

            assertEquals(-1, cb.read(0, 1, bb));
            assertEquals('0', bb.flip().get());
            bb.clear();

            assertEquals(-1, cb.read(0, 2, bb));
            assertEquals('0', bb.flip().get());
            assertEquals('1', bb.get());
            bb.clear();

            assertEquals(2, cb.read(0, 5, bb));
            assertEquals('0', bb.flip().get());
            assertEquals('1', bb.get());
            bb.compact();
            assertEquals(4, cb.read(2, 3, bb));
            assertEquals('2', bb.flip().get());
            assertEquals('3', bb.get());
            bb.compact();
            assertEquals(-1, cb.read(4, 1, bb));
            assertEquals('\n', bb.flip().get());
            bb.compact();

        }
    }

}
