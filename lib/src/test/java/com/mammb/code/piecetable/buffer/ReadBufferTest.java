package com.mammb.code.piecetable.buffer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ReadBufferTest {

    @Test
    void testReadBuffer() {

        var b = ReadBuffer.of("01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8), (short) 10);

        assertEquals(16, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));
        assertEquals("②", new String(b.charAt(2), StandardCharsets.UTF_8));
        assertEquals("Ｆ", new String(b.charAt(15), StandardCharsets.UTF_8));

        assertArrayEquals("01②3456789ABCDEＦ".getBytes(StandardCharsets.UTF_8), b.bytes(0, 20));
        assertEquals("1②34", b.subBuffer(1, 5).toString());

    }
}
