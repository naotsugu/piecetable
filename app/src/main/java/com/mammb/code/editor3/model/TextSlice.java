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
package com.mammb.code.editor3.model;

import com.mammb.code.editor3.lang.StringsBuffer;
import java.util.Objects;

/**
 * TextSlice.
 * @author Naotsugu Kobayashi
 */
public class TextSlice {

    /** The origin row number. */
    private int originRow;

    /** The origin offset(not code point counts). */
    private int originOffset;

    /** The row size of slice. */
    private int maxRowSize;

    /** The text source. */
    private final TextSource source;

    /** The text slice buffer. */
    private final StringsBuffer buffer = new StringsBuffer();


    /**
     * Constructor.
     * @param source the text source
     */
    public TextSlice(TextSource source) {
        this.source = Objects.requireNonNull(source);
        this.originRow = 0;
        this.originOffset = 0;
        this.maxRowSize = 10;
    }


    /**
     *
     * @param offset
     * @param string
     */
    public void insert(int offset, String string) {
        buffer.insert(offset, string);
        source.handle(Edit.insert(offset, string));
        fitRow();
    }


    public void delete(int offset, int length) {
        String deleted = buffer.delete(offset, length);
        source.handle(Edit.delete(offset, deleted));
        fitRow();
    }


    /**
     * Shift the row.
     * @param rowDelta the delta of row
     */
    public void shiftRow(int rowDelta) {
        if (rowDelta > 0) {
            // scroll next (i.e. arrow down)
            String tail = source.afterRow(buffer.length(), rowDelta);
            if (tail.isEmpty()) return;
            source.shiftRow(rowDelta);
            originOffset += buffer.shiftAppend(tail);
        } else if (rowDelta < 0) {
            // scroll prev (i.e. arrow up)
            int cp = source.shiftRow(rowDelta);
            if (cp == 0) return;
            String head = source.rows(Math.abs(rowDelta));
            buffer.shiftInsert(0, head);
            originOffset -= head.length();
        }
        originRow += rowDelta;
    }


    public void refresh() {
        buffer.set(source.rows(maxRowSize));
    }


    private void fitRow() {
        if (maxRowSize > buffer.rowSize() && source.totalRowSize() > maxRowSize) {
            buffer.append(source.afterRow(buffer.length()));
        } else if (buffer.rowSize() > maxRowSize) {
            buffer.truncateRows(buffer.rowSize() - maxRowSize);
        }
    }


    /**
     * Get the string.
     * @return the string
     */
    public String string() {
        return buffer.toString();
    }


    /**
     * Get the row size of slice.
     * @return the row size of slice
     */
    public int maxRowSize() {
        return maxRowSize;
    }


    /**
     * Set the row size of slice.
     * @param maxRowSize the row size of slice
     */
    public void setMaxRowSize(int maxRowSize) {
        this.maxRowSize = maxRowSize;
    }


    /**
     * Get the origin row number.
     * @return the origin row number
     */
    public int originRow() {
        return originRow;
    }


    /**
     * Get the origin offset(not code point counts).
     * @return the origin offset(not code point counts)
     */
    public int originOffset() {
        return originOffset;
    }

}
