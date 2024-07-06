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
package com.mammb.code.piecetable.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link Reader}.
 * @author Naotsugu Kobayashi
 */
class ReaderTest {

    @Test
    void reader(@TempDir Path tempDir) throws IOException {

        var file = tempDir.resolve("file.txt");
        Files.write(file, "a\nbc\ndef\n".getBytes());

        var reader = Reader.of(file);

        assertEquals(0, reader.index().get(0));
        assertEquals(2, reader.index().get(1));
        assertEquals(5, reader.index().get(2));

        assertEquals(StandardCharsets.UTF_8, reader.charset());

    }

    @Test
    void readerUtf16(@TempDir Path tempDir) throws IOException {

        var file = tempDir.resolve("file.txt");
        Files.write(file, "a\nbc\ndef\n".getBytes(StandardCharsets.UTF_16));

        var reader = Reader.of(file);

        assertArrayEquals(new byte[] { (byte) 0xFE, (byte) 0xFF }, reader.bom());

        assertEquals(0, reader.index().get(0));
        assertEquals(4 + 2, reader.index().get(1));
        assertEquals(10 + 2, reader.index().get(2));

        assertEquals(StandardCharsets.UTF_16BE, reader.charset());

    }


}
