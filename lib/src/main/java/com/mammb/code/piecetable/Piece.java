package com.mammb.code.piecetable;

import com.mammb.code.piecetable.buffer.Buffer;
import java.util.Optional;

record Piece(Buffer target, int index, int length) {

    record Pair(Piece left, Piece right) { }

    public Pair split(int offset) {
        if (offset >= length) {
            throw new RuntimeException("Illegal offset value. offset[%s], length[%s]"
                    .formatted(offset, length));
        }
        return new Pair(
                new Piece(target, index, offset),
                new Piece(target, index + offset, length - offset));
    }

    public Optional<Piece> marge(Piece other) {
        if (target == other.target) {
            if (end() == other.index) {
                return Optional.of(new Piece(target, index, length + other.length));
            }
            if (other.end() == index) {
                return Optional.of(new Piece(target, other.index, length + other.length));
            }
        }
        return Optional.empty();
    }

    public int end() {
        return index + length;
    }

}
