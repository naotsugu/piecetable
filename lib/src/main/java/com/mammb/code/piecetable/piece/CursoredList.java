package com.mammb.code.piecetable.piece;

import com.mammb.code.piecetable.array.ByteArray;
import com.mammb.code.piecetable.buffer.Buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Provides cursor-based list access.
 *
 * Traversing LinkedList is very inefficient,
 * so we provide list access by cursor.
 *
 * @author Naotsugu Kobayashi
 */
public class CursoredList {

    private final List<Piece> raw;
    private final ListIterator<Piece> cursor;
    private final PiecePoint point;

    public CursoredList(List<Piece> raw) {
        this.raw = raw;
        this.cursor = raw.listIterator();
        this.point = new PiecePoint();
    }

    public CursoredList() {
        this(new LinkedList<>());
    }

    public static CursoredList of() {
        return new CursoredList();
    }

    public Piece get(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length()));
        }
        moveTo(index);
        return next();
    }

    public PiecePoint at(int pos) {
        if (point.position() < pos) {
            while (cursor.hasNext() && point.position() < pos) {
                next();
            }
            if (point.position() != pos) {
                prev();
            }
            return point.copy();
        }
        if (point.position() > pos) {
            while (cursor.hasPrevious() && point.position() > pos) {
                prev();
            }
        }
        return point.copy();
    }

    public void add(int index, Piece... pieces) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException();
        }
        moveTo(index);
        for (int i = 0; i < pieces.length; i++) {
            var piece = pieces[i];
            cursor.add(piece);
            point.inc(piece.length());
        }
    }

    public void remove(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length()));
        }
        get(index);
        moveTo(index);
        cursor.remove();
    }

    public int length() {
        return raw.size();
    }

    public ByteArray bytes(int startPos, int endPos) {
        ByteArray byteArray = ByteArray.of();
        PiecePoint from = at(startPos);
        PiecePoint to   = at(endPos - 1);
        for (int i = 0; i <= to.index() - from.index(); i++) {
            Piece piece = get(from.index() + i);
            int s = (i == 0) ? startPos - from.position() : 0;
            int e = (i == (to.index() - from.index())) ? endPos - to.position() : piece.length();
            byteArray.add(piece.bytes(s, e).bytes());
        }
        return byteArray;
    }

    public ByteArray bytes(int startPos, Predicate<byte[]> until) {
        ByteArray byteArray = ByteArray.of();
        PiecePoint from = at(startPos);
        int start = startPos - from.position();
        for (int i = from.index(); i < length(); i++) {
            Piece piece = get(i);
            for (;;) {
                int end = Math.min(piece.end(), startPos + 256);
                Buffer buf = piece.bytes(start, end);
                for (int j = 0; j < buf.length(); j++) {
                    byte[] bytes = buf.charAt(j);
                    byteArray.add(bytes);
                    if (until.test(bytes)) {
                        return byteArray;
                    }
                }
                if (end == piece.end()) {
                    break;
                }
                start = end;
            }
            start = 0;
        }
        return byteArray;
    }

    public ByteArray bytesBefore(int startPos, Predicate<byte[]> until) {
        ByteArray byteArray = ByteArray.of();
        PiecePoint from = at(startPos);
        boolean first = true;
        int start = startPos - from.position();
        for (int i = from.index(); i >= 0; i--) {
            Piece piece = get(i);
            if (!first) {
                start = piece.end();
            }
            for (;;) {
                int end = Math.max(piece.bufIndex(), startPos - 256);
                Buffer buf = piece.bytes(end, start);
                for (int j = buf.length() - 1; j >= 0; j--) {
                    byte[] bytes = buf.charAt(j);
                    byteArray.add(bytes);
                    if (until.test(bytes)) {
                        return byteArray.reverse();
                    }
                }
                if (end == piece.bufIndex()) {
                    break;
                }
                start = end;
            }
            first = false;
        }
        return byteArray.reverse();
    }

    private Piece next() {
        if (cursor.hasNext()) {
            var piece = cursor.next();
            point.inc(piece.length());
            return piece;
        }
        return null;
    }

    private Piece prev() {
        if (cursor.hasPrevious()) {
            var piece = cursor.previous();
            point.dec(piece.length());
            return piece;
        }
        return null;
    }

    private void moveTo(int index) {
        while (cursor.hasNext() && cursor.nextIndex() < index)  {
            next();
        }
        while (cursor.hasPrevious() && cursor.nextIndex() > index)  {
            prev();
        }
        if (cursor.nextIndex() != index) {
            throw new IndexOutOfBoundsException();
        }
    }

    public int writeTo(WritableByteChannel channel) throws IOException {
        int size = 0;
        ByteBuffer buf = ByteBuffer.allocate(1024);
        for (Piece piece : raw) {
            size += piece.writeTo(channel, buf);
        }
        return size;
    }

    public Stream<Piece> stream() {
        return raw.stream();
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("point:").append(point.toString()).append("\n");
        raw.forEach(sb::append);
        return sb.toString();
    }

}
