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
import java.nio.file.Path;
import java.util.Objects;

/**
 * TextSlice.
 * @author Naotsugu Kobayashi
 */
public class TextSlice {

    /** The origin point. */
    private RowPoint origin;

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
        this.origin = RowPoint.zero;
        this.maxRowSize = 10;
    }


    /**
     * Open the path content.
     * @param path the content path
     */
    public void open(Path path) {
        this.origin = RowPoint.zero;
        source.as(path);
        refresh();
    }

    public void save() {
        source.save();
    }


    public void saveAs(Path path) {
        source.saveAs(path);
    }


    /**
     * Insert string.
     * @param offset the char offset on buffer
     * @param string the inserting string
     */
    public void insert(int offset, String string) {
        int beforeRowSize = buffer.rowSize();
        buffer.insert(offset, string);
        source.handle(Edit.insert(origin.offset(), offset, string));
        if (hasNext() && beforeRowSize < buffer.rowSize()) {
            buffer.truncateRows(buffer.rowSize() - beforeRowSize);
        }
    }


    /**
     * Delete string.
     * @param offset the char offset(not code point) on buffer
     * @param length the length of deletion
     */
    public void delete(int offset, int length) {
        int beforeRowSize = buffer.rowSize();
        String deleted = buffer.delete(offset, length);
        if (length > buffer.length()) {
            deleted = source.substring(offset, length);
        }
        source.handle(Edit.delete(origin.offset(), offset, deleted));

        if (beforeRowSize > buffer.rowSize()) {
            buffer.append(source.afterRow(buffer.length(), beforeRowSize - buffer.rowSize()));
        }
    }


    /**
     * Peek undo.
     * @return the position of undo
     */
    public OffsetPoint undoPeek() {
        return source.undoPeek().offsetPoint();
    }


    /**
     * Peek redo.
     * @return the position of redo
     */
    public OffsetPoint redoPeek() {
        return source.redoPeek().offsetPoint();
    }


    /**
     * Undo.
     * @return the position of redo
     */
    public OffsetPoint undo() {
        Edit edited = source.undo();
        if (!edited.isEmpty()) {
            refresh();
        }
        return edited.offsetPoint();
    }


    /**
     * Redo.
     * @return the position of redo
     */
    public OffsetPoint redo() {
        Edit edited = source.redo();
        if (!edited.isEmpty()) {
            refresh();
        }
        return edited.offsetPoint();
    }


    /**
     * Shift the row.
     * @param rowDelta the delta of row
     */
    public void shiftRow(int rowDelta) {

        if (rowDelta > 0) {
            // scroll next (i.e. arrow down)
            if (!hasNext()) return;
            String tail = source.afterRow(buffer.length(), rowDelta);
            if (tail.isEmpty()) return;
            int n = source.shiftRow(rowDelta);
            origin = origin.plus(n, buffer.shiftAppend(tail));
        } else if (rowDelta < 0) {
            // scroll prev (i.e. arrow up)
            if (origin.row() == 0) return;
            int n = source.shiftRow(rowDelta);
            if (n == 0) return;
            String head = source.rows(Math.abs(rowDelta));
            buffer.shiftInsert(0, head);
            origin = origin.minus(n, head.length());
        }

    }


    /**
     * Get the sub string.
     * @param offset the char offset(not codepoint) on the slice
     * @param length the char length(not codepoint)
     * @return the string
     */
    public String substring(int offset, int length) {
        return source.substring(offset, length);
    }


    /**
     * Refresh buffer.
     */
    public void refresh() {
        buffer.set(source.rows(maxRowSize));
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
     * Get the origin.
     * @return the origin
     */
    public RowPoint origin() { return origin;  }

    /**
     * Get the origin row number(zero based).
     * @return the origin row number(zero based)
     */
    public int originRow() {
        return origin.row();
    }


    /**
     * Get the origin offset(not code point counts).
     * @return the origin offset(not code point counts)
     */
    public int originOffset() {
        return origin.offset();
    }


    /**
     * Gets whether a subsequent slice exists.
     * @return {@code true} if exists next
     */
    public boolean hasNext() {
        return origin.row() + buffer.rowSize() < source.totalRowSize();
    }


    /**
     * Get the total row size.
     * @return the total row size
     */
    public int totalRowSize() {
        return source.totalRowSize();
    }


    /**
     * Get the content path.
     * @return the content path. {@code null} if content path is empty
     */
    public Path contentPath() {
        return source.contentPath();
    }

}
