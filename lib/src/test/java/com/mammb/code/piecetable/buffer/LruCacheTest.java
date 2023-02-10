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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link LruCache}.
 * @author Naotsugu Kobayashi
 */
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
