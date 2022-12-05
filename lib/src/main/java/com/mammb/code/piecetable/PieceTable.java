package com.mammb.code.piecetable;

import com.mammb.code.piecetable.buffer.AppendBuffer;
import com.mammb.code.piecetable.buffer.Buffer;
import com.mammb.code.piecetable.buffer.Buffers;
import com.mammb.code.piecetable.piece.CursoredList;
import com.mammb.code.piecetable.piece.Piece;
import com.mammb.code.piecetable.piece.PieceEdit;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
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
            var edit = new PieceEdit(point.index(), new Piece[0], new Piece[]{ newPiece });
            pushToUndo(applyEdit(edit), false);
        } else {
            var piece = pieces.get(point.index());
            var pair = piece.split(pos - point.position());
            var edit = new PieceEdit(point.index(), new Piece[] { piece },
                    new Piece[] { pair.left(), newPiece, pair.right() });
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
        var from = pieces.at(pos);
        var to = pieces.at(pos + len - 1);
        var org = new Piece[to.index() - from.index() + 1];
        for (int i = 0; i < org.length; i++) {
            org[i] = pieces.get(from.index() + i);
        }

        // derive the split pieces to be added
        var fromSplit = from.position() != pos;
        var toSplit = (to.position() + org[org.length - 1].length()) != (pos + len);
        var mod = new Piece[btoi(fromSplit) + btoi(toSplit)];
        if (fromSplit) {
            mod[0] = pieces.get(from.index()).split(pos - from.position()).left();
        }
        if (toSplit) {
            mod[btoi(fromSplit)] = org[org.length - 1].split(pos + len - to.position()).right();
        }

        var edit = new PieceEdit(from.index(), org, mod);
        pushToUndo(applyEdit(edit), false);
        length -= len;
    }


    public String substring(int startPos, int endPos) {
        return pieces.substring(startPos, endPos);
    }


    public void undo() {
        if (!undo.isEmpty()) redo.push(applyEdit(undo.pop()));
    }

    public void redo() {
        if (!redo.isEmpty()) pushToUndo(applyEdit(redo.pop()), true);
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

    @Override
    public String toString() {
        return pieces.stream()
            .map(p -> p.target().subBuffer(p.bufIndex(), p.end()).toString())
            .collect(Collectors.joining());
    }

}
