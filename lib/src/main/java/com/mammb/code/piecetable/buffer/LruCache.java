package com.mammb.code.piecetable.buffer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Small LRU cache.
 * @author Naotsugu Kobayashi
 */
class LruCache {

    private final int cacheSize;
    private final Deque<Entry> cache;

    private LruCache(int cacheSize) {
        this.cacheSize = cacheSize;
        this.cache = new ArrayDeque<>(cacheSize);
    }

    static LruCache of() {
        return new LruCache(16);
    }

    void put(int key, int value) {
        var e = new Entry(key, value);
        cache.remove(e);
        if (cache.size() == cacheSize) {
            cache.removeLast();
        }
        cache.addFirst(e);
    }

    Optional<Integer> get(int key) {
        // this cache is very small, so we do iterate.
        for (Entry entry : cache) {
            if (entry.key == key) {
                return Optional.of(entry.value);
            }
        }
        return Optional.empty();
    }

    record Entry(int key, int value) {}

}
