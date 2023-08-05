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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link ChannelBuffer}.
 * @author Naotsugu Kobayashi
 */
class ChannelBufferTest {

    private Path path = Path.of("channelBufferTest.txt");

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(path);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(path);
    }

    @Test
    void testChannelBuffer() throws IOException {

        Files.write(path, "01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8));
        var ch = FileChannel.open(path, StandardOpenOption.READ);

        var b = ChannelBuffer.of(ch, (short) 10);

        assertEquals(16, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));
        assertEquals("②", new String(b.charAt(2), StandardCharsets.UTF_8));
        assertEquals("Ｆ", new String(b.charAt(15), StandardCharsets.UTF_8));

        assertArrayEquals("01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8), b.bytes(0, 20));
        assertEquals("1②34", b.subBuffer(1, 5).toString());

    }

    @Test
    void testChannelBufferWithConsumer() throws IOException {

        var str = "01②3456789あいうえお🌞ABCDEＦ";
        var texts = str.getBytes(StandardCharsets.UTF_8);
        Files.write(path, texts);
        var ch = FileChannel.open(path, StandardOpenOption.READ);

        List<byte[]> ret = new ArrayList<>();
        var b = ChannelBuffer.of(ch, ret::add);
        assertEquals(Character.codePointCount(str, 0, str.length()), b.length());

        assertEquals(str, ret.stream()
            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
            .collect(Collectors.joining()));
    }

}
