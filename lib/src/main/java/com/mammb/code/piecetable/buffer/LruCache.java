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

    static LruCache of(int cacheSize) {
        return new LruCache(cacheSize);
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

    void clear() {
        cache.clear();
    }

    record Entry(int key, int value) {}

}
