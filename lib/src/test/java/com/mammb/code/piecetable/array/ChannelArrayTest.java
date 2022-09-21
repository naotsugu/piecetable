package com.mammb.code.piecetable.array;

import com.mammb.code.piecetable.buffer.ChannelBuffer;
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
    void get() throws IOException {

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
