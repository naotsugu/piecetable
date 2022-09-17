package com.mammb.code.piecetable.piece;

/**
 * Pieces index with piece start position.
 * @author Naotsugu Kobayashi
 */
public class PiecePoint {

    private int index;
    private int position;

    private PiecePoint(int index, int position) {
        this.index = index;
        this.position = position;
    }

    public PiecePoint() {
        this(0, 0);
    }

    public PiecePoint copy() {
        return new PiecePoint(index, position);
    }

    public void inc(int length) {
        index++;
        position += length;
    }

    public void dec(int length) {
        index--;
        position -= length;
    }

    public int index() {
        return index;
    }

    public int position() {
        return position;
    }

    @Override
    public String toString() {
        return "PiecePoint{bufIndex=%d, position=%d}".formatted(index, position);
    }
}
