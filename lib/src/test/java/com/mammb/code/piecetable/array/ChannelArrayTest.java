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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link ChannelArray}.
 * @author Naotsugu Kobayashi
 */
class ChannelArrayTest {

    private Path path = Path.of("test.txt");

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(path);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(path);
    }

    @Test
    void testGet() throws IOException {

        Files.write(path, "1②3456789ABCDE".getBytes(StandardCharsets.UTF_8));

        var ch = ChannelArray.of(FileChannel.open(path, StandardOpenOption.READ));
        assertEquals("1", new String(new byte[] { ch.get(0) }, StandardCharsets.UTF_8));
        assertEquals("②", new String(ch.get(1, 4), StandardCharsets.UTF_8));
        assertEquals("3456789ABCDE", new String(ch.get(4, 16), StandardCharsets.UTF_8));

        ch.clear();
        assertEquals("ABCDE", new String(ch.get(11, 16), StandardCharsets.UTF_8));

        ch.close();
    }
}
