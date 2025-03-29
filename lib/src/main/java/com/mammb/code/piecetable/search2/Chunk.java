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
package com.mammb.code.piecetable.search2;

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
     * Get the length.
     * @return the length
     */
    long length() {
        return to - from;
    }

    /**
     * Get the parent length.
     * @return the parent length
     */
    long parentLength() {
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
    long distance() {
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
    long reverseDistance() {
        return parentTo - from;
    }

}
