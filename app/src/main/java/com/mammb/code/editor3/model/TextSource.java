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

    /** The edit queue. */
    private final EditQueue editQueue;


    /**
     * Constructor.
     * @param source the code point offset index
     */
    public TextSource(Content source) {
        this.source = source;
        this.offset = 0;
        this.editQueue = new EditQueue(source);
    }


    /**
     * Shifts source index.
     * @param charOffset the char offset from this source offset
     */
    public void shiftIndex(int charOffset) {
        if (charOffset == 0) return;
        if (charOffset < 0) throw new IndexOutOfBoundsException("charOffset[" + charOffset + "]");

        int newValue = offset + asCodePointCount(charOffset);
        if (newValue < 0 || newValue > totalSize()) {
            throw new IndexOutOfBoundsException();
        }
        editQueue.flush();
        offset += newValue;
    }


    /**
     * Shifts source index at row.
     * @param rowDelta row delta
     * @return the number of shifted row
     */
    public int shiftRow(int rowDelta) {
        if (rowDelta == 0) return 0;
        editQueue.flush();

        byte[] row = (rowDelta > 0)
            ? source.bytes(offset, Until.lfInclusive(Math.abs(rowDelta)))   // scroll next (i.e. arrow down)
            : source.bytesBefore(offset, Until.lf(Math.abs(rowDelta) + 1)); // scroll prev (i.e. arrow up)

        int rows = 1;
        int count = 0;
        for (byte b : row) {
            if ((b & 0xC0) != 0x80) count++;
            if (b == '\n') rows++;
        }
        if (row[row.length - 1] == '\n') rows--;

        offset += Math.signum(rowDelta) * count;
        return rows;
    }


    /**
     * Get the string.
     * @param charOffset the char offset
     * @param charLength the char length
     * @return the string
     */
    public String substring(int charOffset, int charLength) {
        int offsetCodePoint = asCodePointCount(charOffset);
        editQueue.flush();
        return new String(
            source.bytes(offset + offsetCodePoint, Until.charLength(charLength)),
            charset);
    }


    /**
     * Get the rows from content head.
     * @param rowCount the number of rows to retrieve
     * @return the before row
     */
    String rows(int rowCount) {
        editQueue.flush();
        byte[] tailRow = source.bytes(offset, Until.lfInclusive(rowCount));
        return new String(tailRow, charset);
    }


    /**
     * Get the before row.
     * @return the before row
     */
    String beforeRow() {
        editQueue.flush();
        byte[] headRow = source.bytesBefore(offset, Until.lf(2));
        return new String(headRow, charset);
    }


    /**
     * Get the after row.
     * @param charOffset the char offset
     * @return the row string
     */
    String afterRow(int charOffset) { return afterRow(charOffset, 1); }


    /**
     * Get the after row.
     * @param charOffset the char offset
     * @param n the number of row to get
     * @return the row string
     */
    String afterRow(int charOffset, int n) {
        int offsetCodePoint = asCodePointCount(charOffset);
        editQueue.flush();
        byte[] tailRow = source.bytes(offset + offsetCodePoint, Until.lfInclusive(n));
        return new String(tailRow, charset);
    }


    /**
     * Get the total row size.
     * @return the total row size
     */
    public int totalRowSize() {
        return source.rowSize() + editQueue.pendingLfCountDelta();
    }


    /**
     * Get the total code point counts.
     * @return the total code point counts
     */
    public int totalSize() {
        return source.length() + editQueue.pendingCodePointCountDelta();
    }


    @Override
    public void handle(Edit event) {
        if (event.isEmpty()) {
            return;
        }
        Edit edit = event.withCodePointPosition(offset + asCodePointCount(event.offset()));
        editQueue.push(edit);
    }


    /**
     * Undo.
     * @return the undone edit.
     */
    public Edit undo() {
        return editQueue.undo();
    }


    /**
     * Redo.
     * @return the redone edit.
     */
    public Edit redo() {
        return editQueue.redo();
    }


    /**
     * Get the source index.
     * @return the source index
     */
    public int offset() {
        return offset;
    }


    /**
     * Converts character length to code point length.
     * @param length character length
     * @return code point length
     */
    private int asCodePointCount(int length) {
        return source.count(offset, Until.charLength(length));
    }

}
