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
 * <p>
 * Traversing LinkedList is very inefficient,
 * so we provide list access by cursor.
 * </p>
 *
 * @author Naotsugu Kobayashi
 */
public class PieceList {

    /** The raw list of piece. */
    private final List<Piece> raw;

    /** The cursor of list. */
    private final ListIterator<Piece> cursor;

    /** The point of cursor. */
    private final PiecePoint point;


    /**
     * Constructor.
     * @param raw the raw list of piece
     */
    public PieceList(List<Piece> raw) {
        this.raw = raw;
        this.cursor = raw.listIterator();
        this.point = new PiecePoint();
    }


    /**
     * Constructor.
     */
    public PieceList() {
        this(new LinkedList<>());
    }


    /**
     * Create a new CursoredList.
     * @return a new CursoredList
     */
    public static PieceList of() {
        return new PieceList(new LinkedList<>());
    }


    /**
     * Get the piece.
     * @param index the index
     * @return the piece
     */
    public Piece get(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length()));
        }
        moveTo(index);
        return next();
    }


    /**
     * Move cursor to the specified position.
     * @param pos the specified position
     * @return the moved point
     */
    public PiecePoint at(long pos) {
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


    /**
     * Add the specified element at the specified position in this list.
     * @param index at which the specified element is to be inserted element
     * @param pieces to be inserted
     */
    public void add(int index, Piece... pieces) {
        add(index, false, pieces);
    }


    /**
     * Add the specified element at the specified position in this list
     * and if we can, we will merge.
     * @param index at which the specified element is to be inserted element
     * @param withMerge whether to attempt a merge
     * @param pieces to be inserted
     */
    public void add(int index, boolean withMerge, Piece... pieces) {

        if (pieces == null || pieces.length == 0) {
            return;
        }

        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length()));
        }

        moveTo(index);
        for (int i = 0; i < pieces.length; i++) {
            Piece piece = pieces[i];
            cursor.add(piece);
            point.inc(piece.length());
        }

        if (withMerge) {
            // | a | b |
            //     ^    index : 1  added : | 1 | 2 | 3 |
            //
            // | a | 1 | 2 | 3 | b |
            //                 ^
            int lastPieceIndex = index + pieces.length - 1;
            if (index > 0) {
                boolean merged = mergeWithNext(index - 1);
                if (merged) {
                    //   0    1   2   3
                    // | a1 | 2 | 3 | b |
                    lastPieceIndex--;
                }
            }
            if (pieces.length > 1 && lastPieceIndex < (length() - 1)) {
                mergeWithNext(lastPieceIndex);
            }
        }

    }


    /**
     * Removes the element at the specified position in this list.
     * @param index the index of the element to be removed
     */
    public void remove(int index) {
        remove(index, false);
    }


    /**
     * Removes the element at the specified position in this list
     * and if we can, we will merge.
     * @param index the index of the element to be removed
     * @param withMerge whether to attempt a merge
     */
    public void remove(int index, boolean withMerge) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length()));
        }
        get(index);
        moveTo(index);
        cursor.remove();

        if (index > 0 && index < length() - 1 && withMerge) {
            mergeWithNext(index - 1);
        }

    }


    /**
     * Merge the piece at the specified index with the next piece, if possible.
     * <pre>
     *   index:0  | buffer a | 0 | 2 |
     *   index:1  | buffer a | 2 | 3 |
     *
     *   marge(0)
     *   index:0  | buffer a | 0 | 5 |
     *
     * </pre>
     * @param index the specified index
     * @return {@code true} if merged
     */
    private boolean mergeWithNext(int index) {
        Piece curr = get(index);
        Piece next = get(index + 1);
        var merged = curr.merge(next);
        if (merged.isPresent()) {
            moveTo(index);
            cursor.set(merged.get()); cursor.next();
            next();
            cursor.remove();
            point.fixupPosition(curr.length());
            return true;
        }
        return false;
    }


    /**
     * Gets the number of elements in this list.
     * @return the number of elements in this list
     */
    public int length() {
        return raw.size();
    }


    /**
     * Get the bytes at the specified position.
     * @param startPos the start position
     * @param endPos the end position
     * @return the bytes at the specified position
     */
    public ByteArray bytes(long startPos, long endPos) {
        ByteArray byteArray = ByteArray.of();
        PiecePoint from = at(startPos);
        PiecePoint to   = at(endPos - 1);
        for (int i = 0; i <= to.index() - from.index(); i++) {
            Piece piece = get(from.index() + i);
            long s = (i == 0) ? startPos - from.position() : 0;
            long e = (i == (to.index() - from.index())) ? endPos - to.position() : piece.length();
            byteArray.add(piece.bytes(s, e).bytes());
        }
        return byteArray;
    }


    /**
     * Get the bytes at the specified position.
     * @param startPos the start position
     * @param until the Conditions for Termination
     * @return the bytes at the specified position
     */
    public ByteArray bytes(long startPos, Predicate<byte[]> until) {
        ByteArray byteArray = ByteArray.of();
        PiecePoint from = at(startPos);
        long start = startPos - from.position();
        for (int i = from.index(); i < length(); i++) {
            Piece piece = get(i);
            if (piece.length() == 0) continue;
            for (;;) {
                long end = Math.min(piece.length(), start + 1024);
                Buffer buf = piece.bytes(start, end);
                for (int j = 0; j < buf.length(); j++) {
                    byte[] bytes = buf.charAt(j);
                    if (until.test(bytes)) {
                        return byteArray;
                    }
                    byteArray.add(bytes);
                }
                if (end == piece.length()) {
                    break;
                }
                start = end;
            }
            start = 0;
        }
        return byteArray;
    }


    /**
     * Get the count up to the specified condition.
     * @param startPos the start position
     * @param until the conditions for termination
     * @return the number of count
     */
    public int count(long startPos, Predicate<byte[]> until) {
        int count = 0;
        PiecePoint from = at(startPos);
        long start = startPos - from.position();
        for (int i = from.index(); i < length(); i++) {
            Piece piece = get(i);
            if (piece.length() == 0) continue;
            for (;;) {
                long end = Math.min(piece.length(), start + 1024);
                Buffer buf = piece.bytes(start, end);
                for (int j = 0; j < buf.length(); j++) {
                    byte[] bytes = buf.charAt(j);
                    if (until.test(bytes)) {
                        return count;
                    }
                    count++;
                }
                if (end == piece.length()) {
                    break;
                }
                start = end;
            }
            start = 0;
        }
        return count;
    }


    /**
     * Get the bytes at the specified position.
     * Inspects the forward direction from the designated position
     * @param startPosExclude the start position
     * @param until the conditions for termination
     * @return the bytes at the specified position
     */
    public ByteArray bytesBefore(long startPosExclude, Predicate<byte[]> until) {
        if (startPosExclude <= 0) throw new IndexOutOfBoundsException();
        ByteArray byteArray = ByteArray.of();
        PiecePoint from = at(startPosExclude - 1);
        boolean first = true;
        long start = startPosExclude - from.position();
        for (int i = from.index(); i >= 0; i--) {
            Piece piece = get(i);
            if (piece.length() == 0) continue;
            if (!first) {
                start = piece.length();
            }
            for (;;) {
                long end = Math.max(0, start - 1024);
                Buffer buf = piece.bytes(end, start);
                for (long j = buf.length() - 1; j >= 0; j--) {
                    byte[] bytes = buf.charAt(j);
                    if (until.test(bytes)) {
                        return byteArray.reverse();
                    }
                    for (int k = bytes.length - 1; k >= 0; k--) {
                        byteArray.add(bytes[k]);
                    }
                }
                if (end == 0) {
                    break;
                }
                start = end;
            }
            first = false;
        }
        return byteArray.reverse();
    }


    /**
     * Get the code point index.
     * @param startPos the start position
     * @param until the Conditions for Termination
     * @return the code point index at the specified position
     */
    public long position(long startPos, Predicate<byte[]> until) {
        int count = 0;
        PiecePoint from = at(startPos);
        long start = startPos - from.position();
        for (int i = from.index(); i < length(); i++) {
            Piece piece = get(i);
            if (piece.length() == 0) continue;
            for (;;) {
                long end = Math.min(piece.length(), start + 1024);
                Buffer buf = piece.bytes(start, end);
                for (int j = 0; j < buf.length(); j++) {
                    byte[] bytes = buf.charAt(j);
                    if (until.test(bytes)) {
                        return startPos + count;
                    }
                    count++;
                }
                if (end == piece.length()) {
                    break;
                }
                start = end;
            }
            start = 0;
        }
        return startPos + count;

    }

    /**
     * Get the code point index.
     * Inspects the forward direction from the designated position
     * @param startPosExclude the start position
     * @param until the conditions for termination
     * @return the code point index at the specified position
     */
    public long positionBefore(long startPosExclude, Predicate<byte[]> until) {
        if (startPosExclude <= 0) throw new IndexOutOfBoundsException();
        int count = 0;
        PiecePoint from = at(startPosExclude - 1);
        boolean first = true;
        long start = startPosExclude - from.position();
        for (int i = from.index(); i >= 0; i--) {
            Piece piece = get(i);
            if (piece.length() == 0) continue;
            if (!first) {
                start = piece.length();
            }
            for (;;) {
                long end = Math.max(0, start - 1024);
                Buffer buf = piece.bytes(end, start);
                for (long j = buf.length() - 1; j >= 0; j--) {
                    byte[] bytes = buf.charAt(j);
                    if (until.test(bytes)) {
                        return startPosExclude - count;
                    }
                    count++;
                }
                if (end == 0) {
                    break;
                }
                start = end;
            }
            first = false;
        }
        return startPosExclude - count;

    }


    /**
     * Get the count up to the specified range.
     * @param startPos the start position
     * @param endPos the end position
     * @param predicate the count-up conditions
     * @return the number of count
     */
    public int count(long startPos, long endPos, Predicate<byte[]> predicate) {
        int count = 0;
        PiecePoint from = at(startPos);
        PiecePoint to   = at(endPos - 1);
        for (int i = 0; i <= to.index() - from.index(); i++) {
            Piece piece = get(from.index() + i);
            if (piece.length() == 0) continue;
            long start = (i == 0) ? startPos - from.position() : 0;
            long end = (i == (to.index() - from.index())) ? endPos - to.position() : piece.length();
            for (;;) {
                long eu = Math.min(end, start + 1024);
                Buffer buf = piece.bytes(start, eu);
                for (int j = 0; j < buf.length(); j++) {
                    byte[] bytes = buf.charAt(j);
                    if (predicate.test(bytes)) {
                        count++;
                    }
                }
                if (eu == end) {
                    break;
                }
                start = eu;
            }
        }
        return count;
    }


    /**
     * Writes the piece held by this list to the specified channel.
     * @param channel the destination channel
     * @return the number of bytes written, possibly zero
     * @throws IOException If some other I/O error occurs
     */
    public long writeTo(WritableByteChannel channel) throws IOException {
        long size = 0;
        ByteBuffer buf = ByteBuffer.allocate(1024);
        for (Piece piece : raw) {
            size += piece.writeTo(channel, buf);
        }
        return size;
    }


    /**
     * Get a sequential Stream with this list as its source.
     * @return a sequential stream over the elements in this collection
     */
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
        while (cursor.hasNext() && cursor.nextIndex() < index) {
            next();
        }
        while (cursor.hasPrevious() && cursor.nextIndex() > index) {
            prev();
        }
        if (cursor.nextIndex() != index) {
            throw new IndexOutOfBoundsException();
        }
    }


    /**
     * Get the piece point.
     * @return the piece point
     */
    PiecePoint getPoint() {
        return point;
    }


    /**
     * Get the cursor next index.
     * @return the cursor next index
     */
    int nextIndex() {
        return cursor.nextIndex();
    }


    String dump() {
        var sb = new StringBuilder();
        moveTo(0);
        for (int i = 0; ; i++) {
            var p = next();
            if (p == null) break;
            sb.append(i + ": " + p + "\n");
        }
        return sb.toString();
    }

}
