package com.mammb.code.piecetable.piece;

import java.util.Arrays;
import java.util.Objects;

/**
 * Piece edit.
 * @param index The index of buffer
 * @param org The original pieces
 * @param mod The modified pieces
 * @param place
 */
public record PieceEdit(int index, Piece[] org, Piece[] mod, Place place) {

    public enum Place { HEAD, MID, TAIL }

    public PieceEdit {
        Objects.requireNonNull(org);
        Objects.requireNonNull(mod);
        Objects.requireNonNull(place);
    }

    public PieceEdit flip() {
        return new PieceEdit(index, mod, org, place);
    }

    public int orgSize() {
        return org().length;
    }

    public int modSize() {
        return mod().length;
    }

    public int totalOrgLength() {
        return Arrays.stream(org()).mapToInt(Piece::length).sum();
    }

    public int totalModLength() {
        return Arrays.stream(mod()).mapToInt(Piece::length).sum();
    }

}
