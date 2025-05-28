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
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.PieceTable;
import com.mammb.code.piecetable.search.SearchSource;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Implementation of the {@link SearchSource} interface providing the ability
 * to retrieve and interact with document content using a {@link PieceTable}
 * structure and a {@link RowIndex} for row and column mapping. This implementation
 * supports character encodings with specific {@link Charset} and accounts for
 * byte order marks (BOM) when calculating offsets and lengths.
 * @author Naotsugu Kobayashi
 */
public class SearchSourceImpl implements SearchSource {

    /** The {@link PieceTable}. */
    private final PieceTable pt;

    /** The {@link RowIndex}. */
    private final RowIndex index;

    /** The {@link Charset} of Document. */
    private final Charset charset;

    /** The byte order mark length. */
    private final int bom;

    SearchSourceImpl(PieceTable pt, RowIndex index, Charset charset, int bom) {
        this.pt = pt;
        this.index = index;
        this.charset = charset;
        this.bom = bom;
    }

    @Override
    public Charset charset() {
        return charset;
    }

    @Override
    public long length() {
        return pt.length() - bom;
    }

    @Override
    public long serial(int row, int col) {
        return index.offset(row, asRawCol(row, col));
    }

    @Override
    public int[] pos(long offset) {
        int[] ret = index.pos(offset);
        byte[] bytes = pt.get(index.offset(ret[0], 0) + bom, ret[1]);
        var cb = charset.decode(ByteBuffer.wrap(bytes));
        ret[1] = cb.length();
        return ret;
    }

    @Override
    public long rowFloorOffset(long offset) {
        return index.rowFloorOffset(offset);
    }

    @Override
    public long rowCeilOffset(long offset) {
        return index.rowCeilOffset(offset);
    }

    @Override
    public long bufferRead(long offset, long length, ByteBuffer bb) {
        return pt.read(offset + bom, length, bb);
    }

    @Override
    public void bufferRead(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback) {
        pt.read(offset + bom, limitLength, traverseCallback);
    }

    private int asRawCol(int row, int col) {

        long serial = index.get(row);
        int len = Math.toIntExact(index.get(row + 1) - serial);
        byte[] bytes = pt.get(serial + bom, len);

        var cb = charset.decode(ByteBuffer.wrap(bytes));
        var bb = charset.encode(cb.subSequence(0, Math.min(col, cb.length())));
        return bb.limit();

    }

}
