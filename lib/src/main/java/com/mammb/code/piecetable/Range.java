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
package com.mammb.code.piecetable;

import java.util.Objects;

/**
 * The range of points.
 * @param from the position of from
 * @param to   the position of to
 * @author Naotsugu Kobayashi
 */
public record Range(Pos from, Pos to) {

    /**
     * Constructor.
     * @param from the position of from
     * @param to   the position of to
     */
    public Range {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
    }

    /**
     * Create a new range record.
     * @param from the position of from
     * @param to   the position of to
     * @return a new range record
     */
    public static Range of(Pos from, Pos to) {
        return new Range(from, to);
    }

    /**
     * Create a new range record.
     * @param fromRow the number of from row(zero origin)
     * @param fromCol the byte position on the from row
     * @param toRow the number of to row(zero origin)
     * @param toCol the byte position on the to row
     * @return a new range record
     */
    public static Range of(int fromRow, int fromCol, int toRow, int toCol) {
        return new Range(Pos.of(fromRow, fromCol), Pos.of(toRow, toCol));
    }

    /**
     * Get the min pos.
     * @return the min pos
     */
    public Pos min() {
        return from.compareTo(to) < 0 ? from : to;
    }

    /**
     * Get the max pos.
     * @return the max pos
     */
    public Pos max() {
        return from.compareTo(to) > 0 ? from : to;
    }

    /**
     * Get whether this Range is width zero or not.
     * @return {@code true}, if this Range is width zero
     */
    public boolean isMono() {
        return from.compareTo(to) == 0;
    }

}
