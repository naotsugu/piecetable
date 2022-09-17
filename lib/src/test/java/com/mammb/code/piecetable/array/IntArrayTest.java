package com.mammb.code.piecetable.array;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link IntArray}.
 * @author Naotsugu Kobayashi
 */
class IntArrayTest {

    @Test
    void test() {

        var i = IntArray.of();
        assertEquals(0, i.length());
        assertEquals(0, i.capacity());

        i.add(new int[] { 1,2,3 });
        assertEquals(3, i.length());
        assertEquals(10, i.capacity());
        assertArrayEquals(new int[] { 1,2,3 }, i.get());

        i.add(new int[] { 1,2,3,4,5,6,7,8,9 });
        assertEquals(12, i.length());
        assertEquals(15, i.capacity());

        i.add(new int[] { 1,2,3 });
        assertEquals(15, i.length());
        assertEquals(15, i.capacity());

        i.add(99);
        assertEquals(16, i.length());
        assertEquals(22, i.capacity());
        assertArrayEquals(new int[] { 1,2,3,1,2,3,4,5,6,7,8,9,1,2,3,99 }, i.get());

        i.clear();
        assertEquals(0, i.length());
        assertEquals(0, i.capacity());
        assertArrayEquals(new int[]{}, i.get());

    }
}
