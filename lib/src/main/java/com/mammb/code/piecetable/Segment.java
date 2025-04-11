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
package com.mammb.code.piecetable;

/**
 * Segment.
 * @author Naotsugu Kobayashi
 */
public interface Segment {

    /**
     * Get the fraction amount.
     * @return the fraction amount
     */
    long fraction();

    /**
     * Get the whole amount.
     * @return the whole amount
     */
    long whole();

    /**
     * The values {@link Segment}.
     * @param fraction the fraction amount
     * @param whole the whole amount
     * @param value the value
     * @param <T> the type of value
     */
    record Valued<T>(long fraction, long whole, T value) implements Segment {
    }

    /**
     * Create a new {@link Segment}.
     * @param fraction the fraction amount
     * @param whole the whole amount
     * @return a new {@link Segment}
     */
    static Segment of(long fraction, long whole) {
        record SegmentRecord(long fraction, long whole) implements Segment { }
        return new SegmentRecord(fraction, whole);
    }

    /**
     * Create a new {@link Valued}.
     * @param fraction the fraction amount
     * @param whole the whole amount
     * @param value the value
     * @return a new {@link Valued}
     * @param <T> the type of value
     */
    static <T> Valued<T> valuedOf(long fraction, long whole, T value) {
        return new Valued<>(fraction, whole, value);
    }

}
