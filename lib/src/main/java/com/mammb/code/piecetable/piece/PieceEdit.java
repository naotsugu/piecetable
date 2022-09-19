package com.mammb.code.piecetable.piece;

import java.util.Objects;

public record PieceEdit(int index, Piece[] org, Piece[] mod) {

    public PieceEdit {
        Objects.requireNonNull(org);
        Objects.requireNonNull(mod);
    }

    public PieceEdit flip() {
        return new PieceEdit(index, mod, org);
    }

}
