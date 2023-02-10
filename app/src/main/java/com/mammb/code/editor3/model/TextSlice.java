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

    /** The slice row size. */
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


    public void shiftRow(int rowDelta) {
        if (rowDelta < 0) {
            source.shiftRow(rowDelta);
            buffer.shiftInsert(0, source.rows(1));
        } else {
            String next = source.afterRow(buffer.length());
            source.shiftRow(rowDelta);
            buffer.shiftAppend(next);
        }
        originRow += rowDelta;
    }


    /**
     * Get the string.
     * @return the string
     */
    public String string() {
        return buffer.toString();
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

}
