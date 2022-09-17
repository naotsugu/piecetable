package com.mammb.code.piecetable;

import com.mammb.code.piecetable.buffer.AppendBuffer;
import com.mammb.code.piecetable.buffer.Buffer;
import com.mammb.code.piecetable.buffer.Buffers;
import com.mammb.code.piecetable.piece.CursoredList;
import com.mammb.code.piecetable.piece.Piece;

import java.util.stream.Collectors;

/**
 * Piece Table implementation.
 * @author Naotsugu Kobayashi
 */
public class PieceTable {

    private final CursoredList pieces;
    private final AppendBuffer buffer;
    private int length;

    public PieceTable(Buffer readBuffer, AppendBuffer appendBuffer) {
        this.pieces = CursoredList.of();
        this.pieces.add(0, new Piece(readBuffer, 0, readBuffer.length()));
        this.buffer = appendBuffer;
        this.length = readBuffer.length();
    }

    public static PieceTable of(Buffer readBuffer) {
        return new PieceTable(readBuffer, Buffers.appendOf());
    }

    public static PieceTable of(CharSequence cs) {
        return new PieceTable(Buffers.of(cs), Buffers.appendOf());
    }

    public void insert(int pos, CharSequence cs) {

        if (pos < 0 || pos > length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
        }

        Buffer buf = Buffers.of(cs);
        var newPiece = new Piece(buffer, buffer.length(), buf.length());
        buffer.append(buf);

        var point = pieces.at(pos);
        if (point.position() == pos) {
            pieces.add(point.index(), newPiece);
        } else {
            var piece = pieces.get(point.index());
            var pair = piece.split(pos - point.position());
            pieces.remove(point.index());
            pieces.add(point.index(), pair.left(), newPiece, pair.right());
        }

        length += buf.length();

    }


    public void delete(int pos, int len) {

        if (pos < 0 || pos >= length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
        }
        if (len <= 0) {
            return;
        }

        Piece[] split = new Piece[2];
        var from = pieces.at(pos);
        if (from.position() != pos) {
            split[0] = pieces.get(from.index()).split(pos - from.position()).left();
        }

        var to = pieces.at(pos + len - 1);
        var toPiece = pieces.get(to.index());
        if ((to.position() + toPiece.length()) != (pos + len)) {
            split[1] = toPiece.split(pos + len - to.position()).right();
        }

        for (int i = to.index(); i >= from.index(); i--) {
            pieces.remove(i);
        }
        if (split[1] != null) pieces.add(from.index(), split[1]);
        if (split[0] != null) pieces.add(from.index(), split[0]);

        length -= len;

    }

    @Override
    public String toString() {
        return pieces.stream()
            .map(p -> p.target().subBuffer(p.bufIndex(), p.end()).toString())
            .collect(Collectors.joining());
    }

}
