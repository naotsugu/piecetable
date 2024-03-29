/*
 * Copyright 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.piecetable;

import com.mammb.code.piecetable.buffer.AppendBuffer;
import com.mammb.code.piecetable.buffer.Buffer;
import com.mammb.code.piecetable.buffer.Buffers;
import com.mammb.code.piecetable.piece.PieceList;
import com.mammb.code.piecetable.piece.Piece;
import com.mammb.code.piecetable.piece.PieceEdit;
import com.mammb.code.piecetable.piece.PiecePoint;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Piece Table implementation.
 *
 * <pre>
 *            PieceList pieces
 *            -------------------------------------
 *            | Buffer target | bufIndex | length |
 *            -------------------------------------
 *  (1) Piece |  readBuffer  |       0  |      6  |
 *            -------------------------------------
 *  (2) Piece | appendBuffer |       0  |      3  |
 *            -------------------------------------
 *  (3) Piece |  readBuffer  |       7  |      2  |
 *            -------------------------------------
 *            ...
 *
 *
 *  readBuffer(read only)                  appendBuffer
 *  ---------------------------------      -------------
 *  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |      | 0 | 1 | 2 | ...
 *  | L | o | r | e | m |   | u | m |      | i | p | s | ...
 *  ---------------------------------      -------------
 *  \__________(1)_________/
 *                                         \____(2)___/
 *                          \_(3)__/
 *
 *  (1)                     (2)          (3)
 *  | L | o | r | e | m |   | i | p | s | u | m |
 * </pre>
 *
 * @author Naotsugu Kobayashi
 */
public class PieceTable {

    /** The pieces. */
    private final PieceList pieces;

    /** The Append buffer. */
    private final AppendBuffer buffer;

    /** The length of characters(code point). */
    private long length;

    /** The undo queue. */
    private final Deque<PieceEdit> undo;

    /** The redo queue. */
    private final Deque<PieceEdit> redo;

    /** undo enable?. */
    private boolean undoEnable = false;


    /**
     * Constructor.
     * @param readBuffer the read buffer
     * @param appendBuffer the Append buffer
     */
    public PieceTable(Buffer readBuffer, AppendBuffer appendBuffer) {
        this.pieces = PieceList.of();
        this.pieces.add(0, new Piece(readBuffer, 0, readBuffer.length()));
        this.buffer = appendBuffer;
        this.length = readBuffer.length();
        this.undo = new ArrayDeque<>();
        this.redo = new ArrayDeque<>();
    }


    /**
     * Create a new {@code PieceTable}.
     * @param readBuffer the read buffer
     * @return a new {@code PieceTable}
     */
    public static PieceTable of(Buffer readBuffer) {
        return new PieceTable(readBuffer, Buffers.appendOf());
    }


    /**
     * Create a new {@code PieceTable}.
     * @param cs the char Sequence
     * @return a new {@code PieceTable}
     */
    public static PieceTable of(CharSequence cs) {
        return new PieceTable(Buffers.of(cs), Buffers.appendOf());
    }


    /**
     * Create a new {@code PieceTable}.
     * @param path the path
     * @return a new {@code PieceTable}
     */
    public static PieceTable of(Path path) {
        return new PieceTable(Buffers.of(path), Buffers.appendOf());
    }


    /**
     * Create a new {@code PieceTable}.
     * @param path the path
     * @param traverse the bytes traverse at initial loading
     * @return a new {@code PieceTable}
     */
    public static PieceTable of(Path path, Consumer<byte[]> traverse) {
        return new PieceTable(Buffers.of(path, traverse), Buffers.appendOf());
    }


    /**
     * Inserts the char sequence into this {@code PieceTable}.
     * @param pos the offset
     * @param cs a char sequence
     */
    public void insert(long pos, CharSequence cs) {

        if (pos < 0 || pos > length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
        }

        // append the inserted text to the end of the append buffer
        Buffer buf = Buffers.of(cs);
        Piece newPiece = new Piece(buffer, buffer.length(), buf.length());
        buffer.append(buf);

        PiecePoint point = pieces.at(pos);
        if (point.position() == pos) {
            // add to the boundary position
            PieceEdit edit = new PieceEdit(
                    point.index(),
                    new Piece[0],
                    new Piece[] { newPiece },
                    PieceEdit.Place.HEAD);
            pushToUndo(applyEdit(edit), false);
        } else {
            // split the piece and add
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


    /**
     * Removes the characters in a substring of this {@code PieceTable}.
     * @param pos the beginning index, inclusive
     * @param len the length to be deleted
     */
    public void delete(long pos, int len) {

        if (len <= 0) {
            return;
        }

        if (pos < 0 || pos >= length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
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
            (fromSplit && toSplit) ? PieceEdit.Place.MID
                                   : fromSplit ? PieceEdit.Place.TAIL
                                               : PieceEdit.Place.HEAD);
        pushToUndo(applyEdit(edit), false);
        length -= len;
    }


    /**
     * Get the length of characters(code points) this piece table holds.
     * @return the length of characters(code points)
     */
    public long length() {
        return length;
    }


    /**
     * Gets a new String that contains a subsequence of characters
     * currently contained in this {@code PieceTable}.
     * @param startPos the beginning index, inclusive
     * @param endPos the ending index, exclusive
     * @return the new string
     */
    public String substring(long startPos, long endPos) {
        return new String(pieces.bytes(startPos, endPos).get(), StandardCharsets.UTF_8);
    }


    /**
     * Get the byte array.
     * @param startPos the beginning index, inclusive
     * @param endPos the ending index, exclusive
     * @return the bytes
     */
    public byte[] bytes(long startPos, long endPos) {
        return pieces.bytes(startPos, endPos).get();
    }


    /**
     * Get the bytes.
     * <pre>
     *     | a | b | c | d |
     *       L startPos
     *                   L  until [d] appears [a, b, c]
     * </pre>
     * @param startPos the start position(code point index), inclusive
     * @param until the until predicate, exclusive
     * @return the bytes
     */
    public byte[] bytes(long startPos, Predicate<byte[]> until) {
        return pieces.bytes(startPos, until).get();
    }


    /**
     * Get the bytes.
     * <pre>
     *     | a | b | c | d | a |
     *                       L startPos
     *      L  until [a] appears [a, b, c]
     * </pre>
     * @param startPosExclude start position(code point index), exclusive
     * @param until the until predicate, exclusive
     * @return the bytes
     */
    public byte[] bytesBefore(long startPosExclude, Predicate<byte[]> until) {
        return pieces.bytesBefore(startPosExclude, until).get();
    }


    /**
     * Get the code point index.
     * <pre>
     *     | a | b | c | d |
     *       L startPos
     *                   L  until [d] appears 3
     * </pre>
     * @param startPos the start position(code point index), inclusive
     * @param until the until predicate, exclusive
     * @return the code point index
     */
    public long position(long startPos, Predicate<byte[]> until) {
        return pieces.position(startPos, until);
    }


    /**
     * Get the code point index.
     * <pre>
     *     | a | b | c | d | a |
     *                       L startPos
     *      L  until [a] appears 3
     * </pre>
     * @param startPosExclude start position(code point index), exclusive
     * @param until the until predicate, exclusive
     * @return the code point index
     */
    public long positionBefore(long startPosExclude, Predicate<byte[]> until) {
        return pieces.positionBefore(startPosExclude, until);
    }


    /**
     * Get the count.
     * @param startPos the start position(code point index), inclusive
     * @param until the until predicate, exclusive
     * @return the count
     */
    public int count(long startPos, Predicate<byte[]> until) {
        return pieces.count(startPos, until);
    }


    /**
     * Get the count.
     * @param startPos the start position(code point index), inclusive
     * @param endPos the ending position(code point index), exclusive
     * @param predicate the count predicate
     * @return the count
     */
    public int count(long startPos, long endPos, Predicate<byte[]> predicate) {
        return pieces.count(startPos, endPos, predicate);
    }


    /**
     * Get the count.
     * @param predicate the count predicate
     * @return the count
     */
    public int count(Predicate<byte[]> predicate) {
        return pieces.count(0, length, predicate);
    }


    /**
     * Undo.
     * @return the edit of undo
     */
    public Edited undo() {
        if (undo.isEmpty()) return Edited.empty;
        PieceEdit pieceEdit = undo.pop();
        redo.push(applyEdit(pieceEdit));
        length += pieceEdit.totalModLength() - pieceEdit.totalOrgLength();
        return asEdited(pieceEdit);
    }


    /**
     * redo.
     * @return the edit of redo
     */
    public Edited redo() {
        if (redo.isEmpty()) return Edited.empty;
        PieceEdit pieceEdit = redo.pop();
        pushToUndo(applyEdit(pieceEdit), true);
        length += pieceEdit.totalModLength() - pieceEdit.totalOrgLength();
        return asEdited(pieceEdit);
    }


    /**
     * Writes the contents of the PieceTable to the specified path.
     * @param path the specified path
     */
    public void write(Path path) {
        try (FileChannel channel = FileChannel.open(path,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            long size = pieces.writeTo(channel);
            channel.truncate(size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Enable undo.
     */
    public void enableUndo() {
        undoEnable = true;
    }


    /**
     * Get the content string.
     * @return the content string
     */
    public String getAsString() {
        return pieces.stream()
            .map(p -> p.target().subBuffer(p.bufIndex(), p.end()).toString())
            .collect(Collectors.joining());
    }


    private void pushToUndo(PieceEdit edit, boolean readyForRedo) {
        if (!undoEnable) return;
        undo.push(edit);
        if (!readyForRedo) redo.clear();
    }


    private PieceEdit applyEdit(PieceEdit edit) {
        for (int i = 0; i < edit.org().length; i++) {
            pieces.remove(edit.index());
        }
        if (edit.mod().length > 0) {
            // TODO withMerge op
            //pieces.add(edit.index(), !undoEnable, edit.mod());
            pieces.add(edit.index(), false, edit.mod());
        }
        return edit.flip();
    }


    private Edited asEdited(PieceEdit edit) {

        long from = 0;
        for (int i = 0; i < edit.index(); i++) {
            Piece p = pieces.get(i);
            from += p.length();
        }
        return edit.asEdited(from);
    }


    private int btoi(boolean bool) {
        return bool ? 1 : 0;
    }

}
