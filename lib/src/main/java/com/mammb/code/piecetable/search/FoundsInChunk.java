/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.search;

import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of found items in a specific data chunk.
 * This record associates a list of {@link Found} objects with a {@link Chunk}.
 *
 * @param founds the list of found items identified within the chunk
 * @param chunk the chunk of data where the found items are located
 * @author Naotsugu Kobayashi
 */
public record FoundsInChunk(List<Found> founds, Chunk chunk) {

    /**
     * Reverses the order of the {@code founds} list contained within this {@code FoundsInChunk}.
     * @return the current {@code FoundsInChunk} instance with the reversed order of found items
     */
    FoundsInChunk reverse() {
        Collections.reverse(founds);
        return this;
    }

    /**
     * Gets whether this founds in chunk is empty or not.
     * @return {@code true} if this founds in chunk is empty
     */
    boolean hasFounds() {
        return !founds.isEmpty();
    }

}
