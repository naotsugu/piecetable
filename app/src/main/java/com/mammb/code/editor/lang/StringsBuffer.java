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
package com.mammb.code.editor.lang;

import java.util.stream.IntStream;

/**
 * StringsBuffer.
 * StringBuilder to cache string metrics.
 * @author Naotsugu Kobayashi
 */
public class StringsBuffer {

    /** The string buffer. */
    private final StringBuilder value = new StringBuilder();

    /** The string metrics. */
    private final StringMetrics metrics = new StringMetrics(value);

    /** The cache of row size. */
    private int rowSizeCache = -1;


    /**
     * Sets the text(replace all).
     * @param cs the text
     */
    public void set(CharSequence cs) {
        value.delete(0, value.length());
        value.append((cs == null) ? "" : cs);
        metrics.clear();
        rowSizeCache = -1;
    }


    /**
     * Inserts the specified CharSequence into this sequence.
     * @param offset the offset
     * @param cs the sequence to be inserted
     */
    public void insert(int offset, CharSequence cs) {
        if (offset >= length()) {
            append(cs);
            return;
        }
        value.insert(offset, cs);
        metrics.clear();
        if (rowSizeCache > -1) {
            rowSizeCache += Strings.countLf(cs);
        }
    }


    /**
     * Removes the characters in a substring of this sequence.
     * @param offset the beginning index
     * @param length the length to delete, exclusive
     * @return the deleted string
     */
    public String delete(int offset, int length) {

        if (offset >= value.length()) return "";

        int end = Math.min(offset + length, value.length());
        String deleted = value.substring(offset, end);

        value.delete(offset, end);
        metrics.clear();
        if (rowSizeCache > -1) {
            rowSizeCache -= Strings.countLf(deleted);
        }
        return deleted;
    }


    /**
     * Appends the specified character sequence to this Appendable.
     * @param cs the character sequence to append
     */
    public void append(CharSequence cs) {
        if (cs == null || cs.isEmpty()) return;
        value.append(cs);
        metrics.clear();
        if (rowSizeCache > -1) {
            rowSizeCache += Strings.countLf(cs);
        }
    }


    /**
     * Trim the rows.
     * <pre>
     *    1:  a$  -- max:2 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3: |
     *    4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     *
     *
     *    1:  a$  -- max:3 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3:  c$
     *    4: |                  4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 4
     *
     *
     *    1:  a$  -- max:3 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3:  c$
     *    4:  d                 4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 4
     *
     * </pre>
     * @param max the size of max
     */
    public void trim(int max) {
        if (max < 1 || max >= rowViewSize())  return;
        int offset = rowOffset(max);
        value.delete(offset, value.length());
        metrics.clear();
        rowSizeCache = max + 1;
    }


    /**
     * Trim the rows before.
     * <pre>
     *    1:  a$  -- max:2 -->  2:  b$
     *    2:  b$                3:  c$
     *    3:  c$                4: |
     *    4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     *
     *
     *    1:  a$  -- max:3 -->  1:  a$
     *    2:  b$                2:  b$
     *    3:  c$                3:  c$
     *    4: |                  4: |
     *  ---------------------------------
     *   rowSize 4            rowSize 4
     *
     *
     *    1:  a$  -- max:3 -->  2:  b$
     *    2:  b$                3:  c$
     *    3:  c$                4:  d|
     *    4:  d|
     *  ---------------------------------
     *   rowSize 4            rowSize 3
     *
     * </pre>
     * @param max the size of max
     */
    public void trimBefore(int max) {
        if (max < 1 || max >= rowViewSize())  return;
        int tailGap = tailIsEmpty() ? 1 : 0;
        int offset = rowOffset(rowViewSize() - max - tailGap);
        value.delete(0, offset);
        metrics.clear();
        rowSizeCache = max + tailGap;
    }


    /**
     * Shift row and append text.
     * @param tail append string
     * @return the number of deleted character
     */
    public int shiftAppend(CharSequence tail) {

        int rows = Strings.countLf(tail);
        int len = IntStream.range(0, rows).map(this::rowLength).sum();

        if (len > 0) value.delete(0, len);
        else if (rowSizeCache > -1) rowSizeCache += rows;

        value.append(tail);
        metrics.clear();
        return len;
    }


    /**
     * Shift row and insert text.
     * @param row at the number of row
     * @param cs insertion string
     * @param maxRowSize the size of max
     * @return the number of deleted character
     */
    public int shiftInsert(int row, CharSequence cs, int maxRowSize) {

        if (cs.isEmpty()) return 0;

        value.insert(rowOffset(row), cs);
        metrics.clear();
        if (rowSizeCache > -1) {
            rowSizeCache += Strings.countLf(cs);
        }

        int before = value.length();
        trim(maxRowSize);
        return before - value.length();
    }


    /**
     * Get the character length.
     * @return the length of the sequence of characters currently represented by this object
     */
    public int length() {
        return value.length();
    }


    /**
     * Returns the char value in this sequence at the specified index.
     * @param index the index of the desired char value
     * @return the char value at the specified index
     */
    public char charAt(int index) {
        return value.charAt(index);
    }


    /**
     * Get the row size of this view text.
     * <pre>
     *            1:  a$     1:  a$     1: |
     *            2:  b$     2:  b$
     *            3: |       3:  c|
     *  -------------------------------------
     *  rowViewSize : 3         3         1
     * </pre>
     * @return the row size
     */
    public int rowViewSize() {
        if (rowSizeCache > -1) {
            return rowSizeCache;
        }
        rowSizeCache = metrics().rowViewSize();
        return rowSizeCache;
    }


    /**
     * Get the row index of this text.
     * @param row the number of row. zero origin
     * @return the row index
     */
    public int rowOffset(int row) {
        return (row == 0) ? 0 : metrics().rowOffset(row);
    }


    /**
     * Get the row length.
     * @param row the specified row
     * @return the row length
     */
    public int rowLength(int row) {
        if (row < rowViewSize() - 1) {
            return rowOffset(row + 1) - rowOffset(row);
        } else if (row == rowViewSize() - 1) {
            return value.length() - rowOffset(row);
        } else {
            return 0;
        }
    }


    /**
     * Gets whether the tail is empty line or not.
     * <pre>
     *    1:|        1: a|        1: a$        1: a$
     *    2:         2:           2: |         2: b
     *  ------------------------------------------------
     *    true       false        true         false
     * </pre>
     * @return {@code true}, if the tail is empty line
     */
    public boolean tailIsEmpty() {
        return value.isEmpty() || value.charAt(value.length() - 1) == '\n';
    }


    /**
     * Gets whether the tail is end of line or not.
     * @return {@code true}, if the tail is end of line
     */
    public boolean tailIsEol() {
        return value.charAt(value.length() - 1) == '\n';
    }


    @Override
    public String toString() {
        return value.toString();
    }

    // -- private -------------------------------------------------------------

    /**
     * Get the initialized metrics.
     * @return the initialized metrics
     */
    private StringMetrics metrics() {
        if (metrics.isDisabled()) {
            metrics.init();
        }
        return metrics;
    }

}
