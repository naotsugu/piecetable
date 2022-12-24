package com.mammb.code.piecetable;

import com.mammb.code.piecetable.buffer.AppendBuffer;
import com.mammb.code.piecetable.buffer.Buffer;
import com.mammb.code.piecetable.buffer.Buffers;
import com.mammb.code.piecetable.piece.CursoredList;
import com.mammb.code.piecetable.piece.Piece;
import com.mammb.code.piecetable.piece.PieceEdit;
import com.mammb.code.piecetable.piece.PiecePoint;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 * Piece Table implementation.
 * @author Naotsugu Kobayashi
 */
public class PieceTable {

    private final CursoredList pieces;
    private final AppendBuffer buffer;
    private int length;

    private final Deque<PieceEdit> undo;
    private final Deque<PieceEdit> redo;

    public PieceTable(Buffer readBuffer, AppendBuffer appendBuffer) {
        this.pieces = CursoredList.of();
        this.pieces.add(0, new Piece(readBuffer, 0, readBuffer.length()));
        this.buffer = appendBuffer;
        this.length = readBuffer.length();
        this.undo = new ArrayDeque<>();
        this.redo = new ArrayDeque<>();
    }

    public static PieceTable of(Buffer readBuffer) {
        return new PieceTable(readBuffer, Buffers.appendOf());
    }

    public static PieceTable of(CharSequence cs) {
        return new PieceTable(Buffers.of(cs), Buffers.appendOf());
    }

    public static PieceTable of(Path path) {
        return new PieceTable(Buffers.of(path), Buffers.appendOf());
    }

    public void insert(int pos, CharSequence cs) {

        if (pos < 0 || pos > length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
        }

        Buffer buf = Buffers.of(cs);
        Piece newPiece = new Piece(buffer, buffer.length(), buf.length());
        buffer.append(buf);

        PiecePoint point = pieces.at(pos);
        if (point.position() == pos) {
            PieceEdit edit = new PieceEdit(
                    point.index(),
                    new Piece[0],
                    new Piece[] { newPiece },
                    PieceEdit.Place.HEAD);
            pushToUndo(applyEdit(edit), false);
        } else {
            Piece piece = pieces.get(point.index());
            Piece.Pair pair = piece.split(pos - point.position());
            PieceEdit edit = new PieceEdit(
                    point.index(),
                    new Piece[] { piece },
                    new Piece[] { pair.left(), newPiece, pair.right() },
                    PieceEdit.Place.MID);
            pushToUndo(applyEdit(edit), false);
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

        // derive the pieces to be removed
        PiecePoint from = pieces.at(pos);
        PiecePoint to = pieces.at(pos + len - 1);
        Piece[] org = new Piece[to.index() - from.index() + 1];
        for (int i = 0; i < org.length; i++) {
            org[i] = pieces.get(from.index() + i);
        }

        // derive the split pieces to be removed
        boolean fromSplit = from.position() != pos;
        boolean toSplit = (to.position() + org[org.length - 1].length()) != (pos + len);
        Piece[] mod = new Piece[btoi(fromSplit) + btoi(toSplit)];
        if (fromSplit) {
            mod[0] = pieces.get(from.index()).split(pos - from.position()).left();
        }
        if (toSplit) {
            mod[btoi(fromSplit)] = org[org.length - 1].split(pos + len - to.position()).right();
        }

        PieceEdit edit = new PieceEdit(from.index(), org, mod,
            (fromSplit && toSplit) ? PieceEdit.Place.MID : fromSplit ? PieceEdit.Place.TAIL : PieceEdit.Place.HEAD);
        pushToUndo(applyEdit(edit), false);
        length -= len;
    }

    public int length() {
        return length;
    }

    public String substring(int startPos, int endPos) {
        return new String(pieces.bytes(startPos, endPos).get(), StandardCharsets.UTF_8);
    }

    public byte[] bytes(int startPos, int endPos) {
        return pieces.bytes(startPos, endPos).get();
    }

    public int[] undo() {
        if (!undo.isEmpty()) {
            PieceEdit edit = undo.pop();
            redo.push(applyEdit(edit));
            return asRange(edit);
        }
        return new int[0];
    }

    public int[] redo() {
        if (!redo.isEmpty()) {
            PieceEdit edit = redo.pop();
            pushToUndo(applyEdit(edit), true);
            return asRange(edit);
        }
        return new int[0];
    }

    private void pushToUndo(PieceEdit edit, boolean readyForRedo) {
        undo.push(edit);
        if (!readyForRedo) redo.clear();
    }

    private PieceEdit applyEdit(PieceEdit edit) {
        for (int i = 0; i < edit.org().length; i++) {
            pieces.remove(edit.index());
        }
        if (edit.mod().length > 0) {
            pieces.add(edit.index(), edit.mod());
        }
        return edit.flip();
    }

    private int btoi(boolean bool) {
        return bool ? 1 : 0;
    }

    public void write(Path path) {
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            pieces.writeTo(channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // pieces.join();
        // buffer.clear();
        // undo.clear();
        // redo.clear();
        // if (buffer instanceof Closeable closeable) {
        //     try {
        //         closeable.close();
        //     } catch (IOException e) {
        //         throw new RuntimeException(e);
        //     }
        // }
    }

    // -- just insert
    //  org     mod
    //         |+++|
    //
    // -- split insert
    //  org     mod
    // |---|   |---|
    //         |+++|
    //         |---|
    //
    // -- just delete
    //  org     mod
    // |---|
    //
    // -- head delete
    //  org     mod
    // |---|   |+++|
    // |+++|
    //
    // -- tail delete
    //  org     mod
    // |+++|   |+++|
    // |---|
    //
    // -- split delete
    //  org     mod
    // |+++|   |+++|
    // |---|
    // |+++|   |+++|
    //
    private int[] asRange(PieceEdit edit) {

        int from = 0;
        for (int i = 0; i < edit.index(); i++) {
            Piece p = pieces.get(i);
            from += p.length();
        }

        if (edit.totalOrgLength() < edit.totalModLength()) {
            // insert
            int len = edit.totalModLength() - edit.totalOrgLength();
            if (edit.orgSize() == 0) {
                // just insert or revert just delete
                return new int[] { from, from + len };
            }
            if (edit.orgSize() == 1 && edit.modSize() == 3 && edit.place() == PieceEdit.Place.MID) {
                // split insert
                from += edit.mod()[0].length();
                return new int[] { from, from + len };
            }
            if (edit.orgSize() == 1 && edit.place() == PieceEdit.Place.HEAD) {
                // revert head delete
                return new int[] { from, from + len };
            }
            if (edit.orgSize() == 1 && edit.place() == PieceEdit.Place.TAIL) {
                // revert tail delete
                from += edit.org()[0].length();
                return new int[] { from, from + len };
            }
            if (edit.orgSize() == 2 && edit.place() == PieceEdit.Place.MID) {
                // revert split delete
                from += edit.org()[0].length();
                return new int[] { from, from + len };
            }
        }

        if (edit.totalOrgLength() > edit.totalModLength()) {
            int len = edit.totalOrgLength() - edit.totalModLength();
            if (edit.modSize() == 0) {
                // revert just insert or just delete
                return new int[] { from, from + edit.totalOrgLength() };
            }
            if (edit.modSize() == 1 && edit.orgSize() == 3 && edit.place() == PieceEdit.Place.MID) {
                // revert split insert
                from += edit.org()[0].length();
                return new int[] { from, from + edit.totalOrgLength() };
            }
            if (edit.modSize() == 1 && edit.place() == PieceEdit.Place.HEAD) {
                // head delete
                return new int[] { from, from + len };
            }
            if (edit.modSize() == 1 && edit.place() == PieceEdit.Place.TAIL) {
                // tail delete
                from += edit.mod()[0].length();
                return new int[] { from, from + len };
            }
            if (edit.modSize() == 2 && edit.place() == PieceEdit.Place.MID) {
                // split delete
                from += edit.mod()[0].length();
                return new int[] { from, from + len };
            }
        }
        return new int[0];
    }


    @Override
    public String toString() {
        return pieces.stream()
            .map(p -> p.target().subBuffer(p.bufIndex(), p.end()).toString())
            .collect(Collectors.joining());
    }

}
