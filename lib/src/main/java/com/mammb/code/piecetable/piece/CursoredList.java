package com.mammb.code.piecetable.piece;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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

    public Stream<Piece> stream() {
        return raw.stream();
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("point:" + point.toString() + "\n");
        raw.forEach(sb::append);
        return sb.toString();
    }

}
