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
     * Shift row and append text.
     * @param tail append string
     * @return the number of deleted character
     */
    int shiftAppend(CharSequence tail) {
        int len = IntStream.range(0, Strings.countLf(tail)).map(this::rowLength).sum();
        if (len > 0) text.delete(0, len);
        text.append(tail);
        figure.clear();
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
        text.insert(rowIndex, cs);
        figure.clear();
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
