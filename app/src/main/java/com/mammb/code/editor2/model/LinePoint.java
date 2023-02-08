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
package com.mammb.code.editor2.model;

/**
 * LinePoint.
 * @author Naotsugu Kobayashi
 */
public class LinePoint implements Comparable<LinePoint> {

    /** The number of row. */
    private int row;

    /** The offset index on row. */
    private int offset;

    /**
     * Move to next offset.
     */
    void toNext() {
        offset++;
    }

    /**
     * Move to next row.
     */
    void toNextRow() {
        row++;
    }

    /**
     * Move to next row head.
     */
    void toNextHead() {
        row++;
        offset = 0;
    }

    /**
     * Move to previous offset.
     */
    void toPrev() {
        if (offset <= 0) {
            throw new IllegalStateException();
        }
        offset--;
    }

    /**
     * Move to previous row.
     */
    void toPrevRow() {
        if (row <= 0) {
            throw new IllegalStateException();
        }
        row--;
    }

    /**
     * Move to previous row tail.
     */
    void toPrevTail(int tailOffset) {
        if (row <= 0) {
            throw new IllegalStateException();
        }
        row--;
        offset = tailOffset;
    }

    /**
     * Reset this line point.
     */
    public void reset() {
        row = 0;
        offset = 0;
    }

    /**
     * Get the number of row.
     * @return the number of row
     */
    public int row() {
        return row;
    }

    /**
     * Get the offset index on row.
     * @return the offset index on row
     */
    public int offset() {
        return offset;
    }

    @Override
    public int compareTo(LinePoint o) {
        int compare = Integer.compare(this.row, o.row);
        return (compare == 0)
            ? Integer.compare(this.offset, o.offset)
            : compare;
    }

}
