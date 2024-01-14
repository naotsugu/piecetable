/*
 * Copyright 2022-2024 the original author or authors.
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Small LRU cache.
 * @author Naotsugu Kobayashi
 */
class LruCache {

    /** The size of cache. */
    private final int cacheSize;

    /** The caches. */
    private final Deque<Entry> cache;


    /**
     * Constructor.
     * @param cacheSize the size of cache
     */
    private LruCache(int cacheSize) {
        this.cacheSize = cacheSize;
        this.cache = new ArrayDeque<>(cacheSize);
    }


    /**
     * Create a new LRU cache
     * @return a new LRU cache
     */
    static LruCache of() {
        return new LruCache(16);
    }


    /**
     * Create a new LRU cache
     * @param cacheSize the size of cache
     * @return a new LRU cache
     */
    static LruCache of(int cacheSize) {
        return new LruCache(cacheSize);
    }


    /**
     * Put the entry to this cache.
     * @param key the key of entry
     * @param value the value of entry
     */
    void put(long key, long value) {
        var e = new Entry(key, value);
        cache.remove(e);
        if (cache.size() == cacheSize) {
            cache.removeLast();
        }
        cache.addFirst(e);
    }


    /**
     * Get the entry from this cache.
     * @param key the key of entry
     * @return the entry
     */
    Optional<Long> get(long key) {
        // this cache is very small, so we do iterate.
        for (Entry entry : cache) {
            if (entry.key == key) {
                return Optional.of(entry.value);
            }
        }
        return Optional.empty();
    }


    /**
     * Clear this cache.
     */
    void clear() {
        cache.clear();
    }


    /**
     * Entry
     * @param key the key
     * @param value the value
     */
    record Entry(long key, long value) { }

}
