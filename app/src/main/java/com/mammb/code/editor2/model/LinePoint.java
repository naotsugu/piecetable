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
        if (offset <= 0) throw new IllegalStateException();
        offset--;
    }

    /**
     * Move to previous row.
     */
    void toPrevRow() {
        if (row <= 0) throw new IllegalStateException();
        row--;
    }

    /**
     * Move to previous row tail.
     */
    void toPrevTail(int tailOffset) {
        if (row <= 0) throw new IllegalStateException();
        row--;
        offset = tailOffset;
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
