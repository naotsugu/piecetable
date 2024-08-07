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
package com.mammb.code.piecetable.core;

import com.mammb.code.piecetable.PieceTable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The PieceTable implementation.
 * @author Naotsugu Kobayashi
 */
public class PieceTableImpl implements PieceTable {

    /** The Append buffer. */
    private final AppendBuffer appendBuffer;
    /** The pieces. */
    private final List<Piece> pieces;
    /** The index of pieces. */
    private final TreeMap<Long, PiecePoint> indices;
    /** The total byte length of the piece table. */
    private long length;


    /**
     * Constructor.
     * @param initial the initial piece
     */
    PieceTableImpl(Piece initial) {
        appendBuffer = AppendBuffer.of();
        pieces = new ArrayList<>();
        indices = new TreeMap<>();
        length = 0;
        if (initial != null) {
            pieces.add(initial);
            length = initial.length();
        }
    }


    /**
     * Create a new {@code PieceTable}.
     * @return a new {@code PieceTable}
     */
    public static PieceTableImpl of() {
        return new PieceTableImpl(null);
    }


    /**
     * Create a new {@code PieceTable}.
     * @param path the path
     * @return a new {@code PieceTable}
     */
    public static PieceTableImpl of(Path path) {
        var cb = ChannelBuffer.of(path);
        return new PieceTableImpl(new Piece(cb, 0, cb.length()));
    }


    @Override
    public void insert(long pos, byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            return;
        }

        if (pos < 0 || pos > length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
        }

        var newPiece = new Piece(appendBuffer, appendBuffer.length(), bytes.length);
        appendBuffer.append(bytes);

        PiecePoint point = at(pos);
        if (point == null) {

            pieces.add(newPiece);

        } else if (point.position == pos) {

            // add to the boundary position
            pieces.add(point.tableIndex, newPiece);
            indices.tailMap(point.position).clear();

        } else {

            // split the piece and add
            Piece[] splits = point.piece.split(pos - point.position());
            pieces.remove(point.tableIndex);
            pieces.addAll(point.tableIndex, List.of(splits[0], newPiece, splits[1]));
            indices.tailMap(point.position).clear();

        }
        length += bytes.length;
    }


    @Override
    public void delete(long pos, int len) {

        if (len <= 0) {
            return;
        }

        if (pos < 0 || pos >= length) {
            throw new IndexOutOfBoundsException(
                "pos[%d], length[%d]".formatted(pos, length));
        }

        PiecePoint[] range = range(pos, pos + len - 1);
        for (int i = range.length - 1; i >= 0; i--) {
            // remove all pieces of range
            pieces.remove(range[i].tableIndex);
        }

        // disable indices
        PiecePoint from = range[0];
        indices.tailMap(from.position).clear();

        // derive the split pieces
        PiecePoint to = range[range.length - 1];
        if (to.endPosition() != pos + len) {
            pieces.add(from.tableIndex, to.piece.split(pos + len - to.position)[1]);
        }
        if (from.position != pos) {
            pieces.add(from.tableIndex, from.piece.split(pos - from.position)[0]);
        }

        length -= len;
    }


    @Override
    public byte[] get(long pos, int len) {

        if (len <= 0) return new byte[0];
        PiecePoint[] range = range(pos, pos + len - 1);
        if (range.length == 0) return new byte[0];

        byte[] ret = new byte[len];
        int start = Math.toIntExact(pos - range[0].position);
        int destPos = 0;

        for (PiecePoint pp : range) {
            Piece piece = pp.piece();
            int length = (piece.length() >= start + len) ? len : Math.toIntExact(piece.length()) - start;
            byte[] bytes = piece.bytes(start, length);
            System.arraycopy(bytes, 0, ret, destPos, bytes.length);
            start = 0;
            destPos += bytes.length;
            len -= bytes.length;
        }
        return ret;
    }


    @Override
    public long length() {
        return length;
    }


    @Override
    public void save(Path path) {
        write(path);
        pieces.clear();
        appendBuffer.clear();
        indices.clear();
        var cb = ChannelBuffer.of(path);
        pieces.add(new Piece(cb, 0, cb.length()));
        length = cb.length();
    }


    /**
     * Get the all bytes.
     * @return the all bytes
     */
    public byte[] bytes() {
        ByteArray bytes = ByteArray.of();
        for (Piece piece : pieces) {
            bytes.add(piece.bytes());
        }
        return bytes.get();
    }


    /**
     * Writes the contents of the PieceTable to the specified path.
     * @param path the specified path
     */
    private void write(Path path) {

        try (FileChannel channel = FileChannel.open(path,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {

            ByteBuffer buf = ByteBuffer.allocateDirect(
                Math.toIntExact(Math.min(length, 1024 * 64)));

            long size = 0;
            for (Piece piece : pieces) {
                size += piece.writeTo(channel, buf);
            }
            channel.truncate(size);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private PiecePoint at(long pos) {
        // 0  |x|x|x|  length:3
        // 3  |x|x|    length:2
        // 5  |x|x|    length:2
        PiecePoint piecePoint = Optional.ofNullable(indices.floorEntry(pos))
                    .map(Map.Entry::getValue).orElse(null);
        if (piecePoint == null) {
            return fillToIndices(pos, 0L, 0);
        } else {
            if (piecePoint.contains(pos)) {
                return piecePoint;
            } else {
                return fillToIndices(pos,
                    piecePoint.endPosition(),
                    piecePoint.tableIndex() + 1);
            }
        }
    }

    private PiecePoint[] range(long startPos, long endPos) {
        PiecePoint pp = at(startPos);
        PiecePoint to = at(endPos);
        PiecePoint[] org = new PiecePoint[to.tableIndex - pp.tableIndex + 1];
        org[0] = pp;
        for (int i = 1; i < org.length; i++) {
            org[i] = pp = at(pp.endPosition());;
        }
        return org;
    }

    /**
     * Perform gc.
     */
    public void gc() {
        List<Piece> dest = new ArrayList<>(pieces.size());
        Piece prev = null;
        for (Piece piece : pieces) {
            if (prev == null) {
                prev = piece;
                continue;
            }
            if (prev.target() == piece.target() &&
                prev.end() == piece.bufIndex()) {
                prev = new Piece(
                    prev.target(),
                    prev.bufIndex(),
                    prev.length() + piece.length());
            } else {
                dest.add(prev);
                prev = piece;
            }
        }
        if (prev != null) {
            dest.add(prev);
        }
        pieces.clear();
        pieces.addAll(dest);
        indices.clear();
    }


    private PiecePoint fillToIndices(long pos, long piecePosition, int tableIndex) {
        for (int i = tableIndex; i < pieces.size(); i++) {
            var piece = pieces.get(i);
            var piecePoint = new PiecePoint(piecePosition, i, piece);
            indices.put(piecePoint.position, piecePoint);
            if (piecePoint.contains(pos)) {
                return piecePoint;
            }
            piecePosition += piece.length();
        }
        return null;
    }


    record PiecePoint(Long position, int tableIndex, Piece piece) {
        // excludes
        public long endPosition() {
            return position + piece.length();
        }
        public boolean contains(long pos) {
            return position <= pos && pos < endPosition();
        }
    }

}
