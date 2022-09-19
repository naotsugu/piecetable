package com.mammb.code.piecetable.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LruCacheTest {

    @Test
    void testLruCache() {

        var cache = LruCache.of(3);

        cache.put(1, 1);
        assertEquals(1, cache.get(1).get());

        cache.put(2, 2);
        cache.put(3, 3);
        assertEquals(2, cache.get(2).get());
        assertEquals(3, cache.get(3).get());

        cache.put(4, 4);
        assertEquals(4, cache.get(4).get());
        assertFalse(cache.get(1).isPresent());

        assertEquals(3, cache.get(3).get());
        cache.put(5, 5);
        assertFalse(cache.get(2).isPresent());
        assertEquals(5, cache.get(5).get());

    }

}
