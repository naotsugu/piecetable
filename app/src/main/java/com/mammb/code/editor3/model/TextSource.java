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

import com.mammb.code.editor3.lang.Until;
import com.mammb.code.editor3.lang.EventListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * TextSource.
 *
 * Translates access by character index to access by code point.
 * It manages the offset of the code point from which access to the content originates.
 *
 * @author Naotsugu Kobayashi
 */
public class TextSource implements EventListener<Edit> {

    /** The source charset. */
    public final Charset charset = StandardCharsets.UTF_8;

    /** The source text content. */
    private Content source;

    /** The code point offset index on the source. */
    private int offset;

    /** The buffered edit. */
    private Edit bufferedEdit = Edit.empty;


    /**
     * Constructor.
     * @param source the code point offset index
     */
    public TextSource(Content source) {
        this.source = source;
        this.offset = 0;
    }


    /**
     * Get the source index.
     * @return the source index
     */
    public int offset() {
        return offset;
    }


    /**
     * Shifts source index.
     * @param charOffset the char offset
     */
    public void shiftIndex(int charOffset) {
        int newValue = offset + asCodePointCount(charOffset);
        if (newValue < 0 || newValue > source.length()) {
            throw new IndexOutOfBoundsException();
        }
        flush();
        offset += newValue;
    }


    /**
     * Shifts source index at row.
     * @param rowDelta row delta
     */
    public void shiftRow(int rowDelta) {
        if (rowDelta == 0) return;
        flush();
        int count = 0;
        if (rowDelta > 0) {
            byte[] row = source.bytes(offset, Until.lfInclusive(Math.abs(rowDelta)));
            for (byte b : row) if ((b & 0xC0) != 0x80) count++;
        } else {
            byte[] row = source.bytesBefore(offset, Until.lf(Math.abs(rowDelta) + 1));
            for (byte b : row) if ((b & 0xC0) != 0x80) count--;
        }
        offset += count;
    }


    /**
     * Get the rows from content head.
     * @param rowCount the number of rows to retrieve
     * @return the before row
     */
    String rows(int rowCount) {
        flush();
        byte[] tailRow = source.bytes(offset, Until.lfInclusive(rowCount));
        return new String(tailRow, charset);
    }


    /**
     * Get the before row.
     * @return the before row
     */
    String beforeRow() {
        flush();
        byte[] headRow = source.bytesBefore(offset, Until.lf(2));
        return new String(headRow, charset);
    }


    /**
     * Get the after row.
     * @param charOffset the char offset
     * @return the row string
     */
    String afterRow(int charOffset) {
        int offsetCodePoint = asCodePointCount(charOffset);
        flush();
        byte[] tailRow = source.bytes(offset + offsetCodePoint, Until.lfInclusive());
        return new String(tailRow, charset);
    }


    /**
     * Get the total row size.
     * @return the total row size
     */
    public int totalRowSize() {
        return source.rowSize();
    }


    @Override
    public void handle(Edit event) {
        if (event.isEmpty()) {
            return;
        }
        Edit edit = event.withPosition(offset + asCodePointCount(event.position()));
        if (bufferedEdit.canMarge(edit)) {
            bufferedEdit = bufferedEdit.marge(edit);
        } else {
            flush();
            bufferedEdit = edit;
        }
    }


    /**
     * Converts character length to code point length.
     * @param length character length
     * @return code point length
     */
    private int asCodePointCount(int length) {
        return source.count(offset, Until.charLength(length));
    }


    /**
     * Flush buffered edit.
     */
    private void flush() {
        if (bufferedEdit.isEmpty()) {
            return;
        }
        source.handle(bufferedEdit);
        bufferedEdit = Edit.empty;
    }

}
