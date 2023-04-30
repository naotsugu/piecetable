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
package com.mammb.code.editor.model;

/**
 * RowPoint.
 * @param row the origin row number(zero based)
 * @param offset the origin offset(not code point counts)
 */
public record RowPoint(int row, int offset) {

    /** zero. */
    public static RowPoint zero = new RowPoint(0, 0);


    /**
     * Plus point.
     * @param plusRow the plus row value
     * @param plusOffset the plus offset value
     * @return the new RowPoint
     */
    public RowPoint plus(int plusRow, int plusOffset) {
        return new RowPoint(row + plusRow, offset + plusOffset);
    }


    /**
     * Minus point.
     * @param minusRow the minus row value
     * @param minusOffset the minus offset value
     * @return the new RowPoint
     */
    public RowPoint minus(int minusRow, int minusOffset) {
        return new RowPoint(row - minusRow, offset - minusOffset);
    }

}
