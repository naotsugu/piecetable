package com.mammb.dev.picetable.index;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineIndexTest {

    @Test
    void put() {
        var index = new LineIndex();
        for (int i = 0, j = 0; i < 200; i++, j += 10) {
            index.put(i, j);
        }
        assertEquals(0, index.get(0));
        assertEquals(10, index.get(1));
        assertEquals(990, index.get(99));
        assertEquals(1000, index.get(100));

    }
}
