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
 * The position length record.
 * @param row the number of row(zero origin)
 * @param col the byte position on the row
 * @param len the byte length on UTF-16
 */
public record PosLen(int row, int col, int len) {

    /**
     * Canonical constructor.
     */
    public PosLen {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException(String.format("(%d,%d)", row, col));
        }
    }

    /**
     * Create a new position length record.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row
     * @return a new position length record
     */
    public static PosLen of(int row, int col, int len) {
        return new PosLen(row, col, len);
    }

    /**
     * Create a new position length record.
     * @param pos the position record
     * @param len the byte length on UTF-16
     * @return a new position length record
     */
    public static PosLen of(Pos pos, int len) {
        return new PosLen(pos.row(), pos.col(), len);
    }

    /**
     * Get a new position record.
     * @return a new position record
     */
    public Pos pos() {
        return new Pos(row, col);
    }

}
