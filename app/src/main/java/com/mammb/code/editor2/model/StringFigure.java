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

import java.util.stream.IntStream;

/**
 * StringFigure.
 * @author Naotsugu Kobayashi
 */
public class StringFigure {

    /** The text view buffer. */
    private final StringBuilder text = new StringBuilder();

    /** The text figures. */
    private final Figure figure = new Figure();


    /**
     * Sets the text.
     * @param cs the text
     */
    public void set(CharSequence cs) {
        text.delete(0, text.length());
        append(cs);
    }

    /**
     * Appends the specified character sequence to this Appendable.
     * @param cs the character sequence to append
     */
    public void append(CharSequence cs) {
        text.append(cs);
        figure.clear();
    }

    /**
     * Inserts the specified CharSequence into this sequence.
     * @param offset the offset
     * @param cs the sequence to be inserted
     */
    public void insert(int offset, CharSequence cs) {
        text.insert(offset, cs);
        figure.clear();
    }

    /**
     * Shift row and append text.
     * @param tail append string
     * @return the number of deleted character
     */
    int shiftAppend(CharSequence tail) {
        int len = IntStream.range(0, Strings.countLf(tail)).map(this::rowLength).sum();
        if (len > 0) text.delete(0, len);
        append(tail);
        return len;
    }

    /**
     * Shift row and insert text.
     * @param cs insert string
     * @return the number of deleted character
     */
    int shiftInsert(int row, CharSequence cs) {
        int rowIndex = rowIndex(row);
        int len = IntStream.range(rowSize() - Strings.countLf(cs), rowSize()).map(this::rowLength).sum();
        if (len > 0) text.delete(text.length() - len, text.length());
        insert(rowIndex, cs);
        return len;
    }

    /**
     * Get the character length.
     * @return the length of the sequence of characters currently represented by this object
     */
    public int length() {
        return text.length();
    }

    /**
     * Returns the char value in this sequence at the specified index.
     * @param index the index of the desired char value
     * @return the char value at the specified index
     */
    public char charAt(int index) {
        return text.charAt(index);
    }

    /**
     * Get the row size of this view text.
     * @return the row size
     */
    public int rowSize() {
        return figure().rowSize();
    }

    /**
     * Get the row index of this text.
     * @param row the number of row. zero origin
     * @return the row index
     */
    public int rowIndex(int row) {
        return (row == 0) ? 0 : figure().rowIndex(row);
    }

    /**
     * Get the row length.
     * @param row the specified row
     * @return the row length
     */
    public int rowLength(int row) {
        if (row < rowSize() - 1) {
            return rowIndex(row + 1) - rowIndex(row);
        } else if (row == rowSize() - 1) {
            return text.length() - rowIndex(row);
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of Unicode code points in the specified range of this view text.
     * @param beginIndex the index to the first char of the text range
     * @param endIndex the index after the last char of the text range
     * @return the number of Unicode code points in the specified text range
     */
    public int codePointCount(int beginIndex, int endIndex) {
        return text.codePointCount(beginIndex, endIndex);
    }

    /**
     * Get the code point count.
     * @return the code point count
     */
    public int codePointCount() {
        return figure().codePointCount();
    }

    /**
     * Get the row code point count.
     * @param row the specified row
     * @return the row code point count
     */
    public int rowCodePointCount(int row) {
        if (row < rowSize() - 1) {
            return codePointCount(rowIndex(row), rowIndex(row + 1));
        } else if (row == rowSize() - 1) {
            return codePointCount(rowIndex(row), text.length());
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return text.toString();
    }

    // -- private -------------------------------------------------------------

    /**
     * Get the initialized figure.
     * @return the initialized figure
     */
    private Figure figure() {
        if (figure.isDisabled()) {
            figure.init(text);
        }
        return figure;
    }

}
