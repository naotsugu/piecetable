package com.mammb.code.piecetable.buffer;

import com.mammb.code.piecetable.array.ChannelArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("0", new String(b.charAt(0)));
        assertEquals("1", new String(b.charAt(1)));
        assertEquals("②", new String(b.charAt(2)));
        assertEquals("Ｆ", new String(b.charAt(15)));

        assertArrayEquals("01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8), b.bytes(0, 20));
        assertEquals("1②34", b.subBuffer(1, 5).toString());

    }
}
