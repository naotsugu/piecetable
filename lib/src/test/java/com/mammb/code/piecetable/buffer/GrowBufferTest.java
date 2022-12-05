package com.mammb.code.piecetable.buffer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GrowBufferTest {

    @Test
    void testGrowBuffer() {

        var b = GrowBuffer.of((short) 10);

        b.append("01");
        assertEquals(2, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));

        assertArrayEquals("01".getBytes(StandardCharsets.UTF_8), b.bytes(0, 2));
        assertEquals("01", b.subBuffer(0, 2).toString());

        b.append("②3456789");
        assertEquals(10, b.length());
        assertEquals("0", new String(b.charAt(0), StandardCharsets.UTF_8));
        assertEquals("1", new String(b.charAt(1), StandardCharsets.UTF_8));
        assertEquals("②", new String(b.charAt(2), StandardCharsets.UTF_8));
        assertEquals("9", new String(b.charAt(9), StandardCharsets.UTF_8));

        assertArrayEquals("②3456789".getBytes(StandardCharsets.UTF_8), b.bytes(2, 12));
        assertEquals("②3456789", b.subBuffer(2, 10).toString());
    }
}
