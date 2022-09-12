package com.mammb.code.piecetable;

import com.mammb.code.piecetable.buffer.AppendableBuffer;
import com.mammb.code.piecetable.buffer.Buffer;
import com.mammb.code.piecetable.buffer.Buffers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PieceTable {

    private final List<Piece> pieces;
    private final AppendableBuffer buffer;
    private int length;

    private PieceTable(Buffer readBuffer, AppendableBuffer appendBuffer) {
        this.pieces = new LinkedList<>();
        this.pieces.add(new Piece(readBuffer, 0, readBuffer.length()));
        this.buffer = appendBuffer;
        this.length = readBuffer.length();
    }

    public static PieceTable of(Buffer readBuffer, AppendableBuffer appendBuffer) {
        return new PieceTable(readBuffer, appendBuffer);
    }

    public static PieceTable of(CharSequence cs) {
        return new PieceTable(Buffers.of(cs), Buffers.appendableOf());
    }

    public static PieceTable of(byte[] bytes) {
        return new PieceTable(Buffers.of(bytes), Buffers.appendableOf());
    }

    public void insert(int pos, CharSequence cs) {
        if (pos > length) {
            throw new IllegalArgumentException();
        }
        Buffer buf = Buffers.of(cs);
        var newPiece = new Piece(buffer, buffer.length(), buf.length());
        buffer.append(buf);
        var loc = location(pos);
        if (loc.offset == 0) {
            pieces.add(loc.pieceIndex, newPiece);
        } else {
            var piece = pieces.get(loc.pieceIndex);
            var pair = piece.split(loc.offset);
            pieces.remove(piece);
            add(loc.pieceIndex, pair.right(), newPiece, pair.left());
        }
        length += buf.length();
    }

    public void delete(int pos, int len) {
        if (len <= 0) {
            return;
        }
        if (pos >= length) {
            throw new IllegalArgumentException();
        }
        List<Piece> deleting = new ArrayList<>();
        var loc = location(pos);
        var del = -loc.offset;
        for (var i = loc.pieceIndex; i < pieces.size(); i++) {
            var piece = pieces.get(i);
            del += piece.length();
            deleting.add(piece);
            if (del >= len) {
                break;
            }
        }
        pieces.removeAll(deleting);
        if (del > len) {
            var edge = deleting.get(deleting.size() - 1);
            add(loc.pieceIndex, edge.split(edge.length() - (del - len)).right());
        }
        if (loc.offset != 0) {
            var edge = deleting.get(0);
            add(loc.pieceIndex, edge.split(loc.offset).left());
        }
        length -= len;

    }

    private void add(int index, Piece... add) {
        if (Objects.isNull(add) || add.length == 0) {
            return;
        }
        Arrays.asList(add).forEach(e -> pieces.add(index, e));
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        for (Piece piece : pieces) {
            sb.append("[%s] index[%s], length[%s] %s".formatted(
                    (piece.target() instanceof AppendableBuffer) ? "A" : "R",
                    piece.index(),
                    piece.length(),
                    piece.target().subBuffer(piece.index(), piece.end()).toString()));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return pieces.stream()
                .map(p -> p.target().subBuffer(p.index(), p.end()).toString())
                .collect(Collectors.joining());
    }

    private Location location(int pos) {
        var offset = 0;
        for (var i = 0; i < pieces.size(); i++) {
            var piece = pieces.get(i);
            offset += piece.length();
            if (pos < offset) {
                return new Location(i, piece.length() - (offset - pos));
            }
            if (pos == offset) {
                return new Location(i + 1, 0);
            }
        }
        return new Location(pieces.size(), 0);
    }

    private record Location(int pieceIndex, int offset) { }
}
