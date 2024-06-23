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
package com.mammb.dev.picetable.core;

import com.mammb.dev.picetable.PieceTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PieceTableImpl implements PieceTable {
    private final AppendBuffer appendBuffer;
    private final List<Piece> pieces;
    private final TreeMap<Long, PiecePoint> indices;
    private long length;

    public PieceTableImpl() {
        this.appendBuffer = AppendBuffer.of();
        this.pieces = new ArrayList<>();
        this.indices = new TreeMap<>();
        this.length = 0;
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
            long offset = pos - point.position();
            var head = new Piece(point.piece.target(), point.piece.bufIndex(), offset);
            var tail = new Piece(point.piece.target(), point.piece.bufIndex() + offset, point.piece.length() - offset);
            pieces.remove(point.tableIndex);
            pieces.addAll(point.tableIndex, List.of(head, newPiece, tail));
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

        PiecePoint point = at(pos);


        length -= len;
    }

    @Override
    public long length() {
        return length;
    }

    public byte[] bytes() {
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
        Map.Entry<Long, PiecePoint> entry = indices.ceilingEntry(pos);
        if (entry == null) {
            return fillToIndices(pos, 0L, 0);
        } else {
            if (pos < entry.getValue().endPosition()) {
                return entry.getValue();
            } else {
                return fillToIndices(pos,
                    entry.getValue().endPosition(),
                    entry.getValue().tableIndex + 1);
            }
        }
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
        public long endPosition() {
            return position + piece.length();
        }
        public boolean contains(long pos) {
            return position <= pos && pos < endPosition();
        }
    }

}
