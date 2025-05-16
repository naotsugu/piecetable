/*
 * Copyright 2022-2025 the original author or authors.
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
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Implementation of the Piece Table data structure for efficient text editing operations.
 * The Piece Table allows for efficient insert, delete, and read operations by maintaining
 * references to original and modified data without directly modifying the underlying storage.
 * @author Naotsugu Kobayashi
 */
public class PieceTableImpl implements PieceTable {

    /** The source path. */
    private Path sourcePath;
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
     * @param path the source path
     * @param appendBuffer the append buffer
     * @param initial the initial piece
     */
    PieceTableImpl(Path path, AppendBuffer appendBuffer, Piece initial) {
        this.sourcePath = path;
        this.appendBuffer = appendBuffer;
        this.pieces = new ArrayList<>();
        this.indices = new TreeMap<>();
        this.length = 0;
        if (initial != null) {
            this.pieces.add(initial);
            this.length = initial.length();
        }
    }

    /**
     * Create a new {@code PieceTable}.
     * @return a new {@code PieceTable}
     */
    public static PieceTableImpl of() {
        return new PieceTableImpl(null, AppendBuffer.of(), null);
    }

    /**
     * Create a new {@code PieceTable}.
     * @param path the source path
     * @return a new {@code PieceTable}
     */
    public static PieceTableImpl of(Path path) {
        var cb = ChannelBuffer.of(path);
        return new PieceTableImpl(path, AppendBuffer.of(), new Piece(cb, 0, cb.length()));
    }

    /**
     * Create a new {@code PieceTable}.
     * @param bytes the initial byte array
     * @return a new {@code PieceTable}
     */
    public static PieceTableImpl of(byte[] bytes) {
        var buffer = AppendBuffer.of(bytes);
        return new PieceTableImpl(null, buffer, new Piece(buffer, 0, bytes.length));
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
        try {
            if (sourcePath != null &&
                Files.exists(path) &&
                Objects.equals(sourcePath.toRealPath(), path.toRealPath())) {

                Path tmp = Files.createTempFile(sourcePath.getParent(), sourcePath.getFileName().toString(), null);
                write(tmp);
                for (Piece piece : pieces) {
                    if (piece.target() instanceof Closeable closeable) {
                        closeable.close();
                    }
                }

                // we don't use `Files.copy(tmp, sourcePath, ...);`
                // because the icon position on the OS changes
                try (FileChannel inCh = new FileInputStream(tmp.toFile()).getChannel();
                     FileChannel outCh = new FileOutputStream(sourcePath.toFile()).getChannel()) {
                    long position = 0L;
                    long size = inCh.size();
                    while (position < size) {
                        long ret = inCh.transferTo(position, inCh.size(), outCh);
                        position += ret;
                    }
                    outCh.truncate(size);
                }
                Files.delete(tmp);
            } else {
                write(path);
                for (Piece piece : pieces) {
                    if (piece.target() instanceof Closeable closeable) {
                        closeable.close();
                    }
                }
                sourcePath = path;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pieces.clear();
        appendBuffer.clear();
        indices.clear();

        var cb = ChannelBuffer.of(path);
        pieces.add(new Piece(cb, 0, cb.length()));
        length = cb.length();

    }

    @Override
    public void write(Path path) {

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

    @Override
    public void read(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback) {
        var bb = ByteBuffer.allocateDirect(1024 * 512);
        long len = 0;
        for (Piece piece : pieces) {
            if ((len + piece.length()) < offset) continue;
            long start = 0;
            if (len < offset && offset <= len + piece.length()) {
                start = offset - len;
            }
            for (long i = start; i >= 0;) {
                int before = bb.remaining();
                i = piece.read(i, limitLength, bb);
                limitLength -= (before - bb.remaining());
                if (!traverseCallback.apply(bb)) {
                    return;
                }
            }
            len += piece.length();
        }
    }

    @Override
    public long read(long offset, long length, ByteBuffer bb) {
        long limit = length;
        long len = 0;
        for (Piece piece : pieces) {
            if ((len + piece.length()) < offset) continue;
            long start = 0;
            if (len < offset && offset <= len + piece.length()) {
                start = offset - len;
            }
            int before = bb.remaining();
            piece.read(start, limit, bb);
            limit -= (before - bb.remaining());
            if (limit <= 0 || !bb.hasRemaining()) break;
            len += piece.length();
        }
        return length - limit;
    }

    /**
     * Get the all bytes.
     * @return the all bytes
     */
    byte[] bytes() {
        ByteArray bytes = ByteArray.of();
        for (Piece piece : pieces) {
            bytes.add(piece.bytes());
        }
        return bytes.get();
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
