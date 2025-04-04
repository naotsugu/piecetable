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

import java.util.ArrayList;
import java.util.List;

/**
 * The chunk.
 * @param from the from position
 * @param to the to position
 * @param parentFrom the parent from
 * @param parentTo the parent to
 * @author Naotsugu Kobayashi
 */
public record Chunk(long from, long to, long parentFrom, long parentTo) {

    /**
     * Canonical constructor.
     * @param from the from position
     * @param to the to position
     * @param parentFrom the parent from
     * @param parentTo the parent to
     */
    public Chunk {
        if (from > to)
            throw new IllegalArgumentException(String.format("(%d, %d)", from, to));
        if (parentFrom > parentTo)
            throw new IllegalArgumentException(String.format("(%d, %d)", parentFrom, parentTo));
    }

    /**
     * Get the length.
     * @return the length
     */
    public long length() {
        return to - from;
    }

    /**
     * Get the parent length.
     * @return the parent length
     */
    public long parentLength() {
        return parentTo - parentFrom;
    }

    /**
     * <pre>
     * 0 ---------------------------- ┬
     *                                │
     * parentFrom -- from ─┐          ┼
     *                     │          │
     *                 to ─┘ ─┐ from  │ to - parentFrom
     *                        │       │
     *                       ─┘ to    ┴
     *
     * parentTo ----
     * </pre>
     * @return the distance
     */
    public long distance() {
        return to - parentFrom;
    }

    /**
     * <pre>
     * parentFrom 0 ----------------- ┬
     *                                │
     *                       ─┐ from  ┼
     *                        │       │
     *               from ─┐ ─┘ to    │ parentTo - from
     *                     │          │
     * parentTo ----   to ─┘          ┴
     *
     * </pre>
     * @return the reverse distance
     */
    public long reverseDistance() {
        return parentTo - from;
    }


    static List<Chunk> of(SerialSource source, int fromRow, int fromCol, int size) {
        List<Chunk> chunks = new ArrayList<>();
        long from = source.serial(fromRow, fromCol);
        long parentFrom = from;
        long parentTo = source.length();
        while (true) {
            long to = source.rowFloorOffset(from + size);
            chunks.add(new Chunk(from, to, parentFrom, parentTo));
            if (to >= parentTo) break;
            from = to;
        }
        return chunks;
    }

    static List<Chunk> backwardOf(SerialSource source, int fromRow, int fromCol, int size) {
        List<Chunk> chunks = new ArrayList<>();
        long from = source.serial(fromRow, fromCol);
        long parentFrom = from;
        while (true) {
            long to = source.rowCeilOffset(from - size);
            chunks.add(new Chunk(to, from, 0, parentFrom));
            if (to == 0) break;
            from = to;
        }
        return chunks;
    }

}
