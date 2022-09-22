package com.mammb.code.piecetable.piece;

import java.util.Objects;

/**
 * Piece edit.
 * @param index The index of buffer
 * @param org The original pieces
 * @param mod The modified pieces
 */
public record PieceEdit(int index, Piece[] org, Piece[] mod) {

    public PieceEdit {
        Objects.requireNonNull(org);
        Objects.requireNonNull(mod);
    }

    public PieceEdit flip() {
        return new PieceEdit(index, mod, org);
    }

}
