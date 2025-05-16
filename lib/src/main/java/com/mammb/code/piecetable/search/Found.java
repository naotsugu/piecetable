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

/**
 * Represents a structure containing information about a found item in a
 * searchable sequence with its offset and length details.
 *
 * @param offset the starting position of the found item
 * @param len the logical length of the found item
 * @param rawLen the raw length including additional formatting or padding
 * @author Naotsugu Kobayashi
 */
public record Found(long offset, int len, int rawLen) implements Comparable<Found> {

    /**
     * Gets whether this found is empty or not.
     * @return {@code true} if this found is empty
     */
    public boolean isEmpty() {
        return len <= 0;
    }

    /**
     * Get the offset end.
     * @return the offset end
     */
    public long offsetEnd() {
        return offset + rawLen;
    }

    @Override
    public int compareTo(Found that) {
        int c = Long.compare(this.offset, that.offset);
        return c == 0 ? Integer.compare(this.len, that.len) : c;
    }

}
