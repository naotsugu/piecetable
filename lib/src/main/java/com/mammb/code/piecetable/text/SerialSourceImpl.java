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
import com.mammb.code.piecetable.search2.SerialSource;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * The serial source implementation.
 * @author Naotsugu Kobayashi
 */
public class SerialSourceImpl implements SerialSource {

    /** The {@link PieceTable}. */
    private final PieceTable pt;

    /** The {@link Charset} of Document. */
    private final Charset charset;

    /** The {@link RowIndex}. */
    private final RowIndex index;

    /** The byte order mark length. */
    private final int bom;

    SerialSourceImpl(PieceTable pt, Charset charset, RowIndex index, int bom) {
        this.pt = pt;
        this.charset = charset;
        this.index = index;
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
        return index.serial(row, col);
    }

    @Override
    public long rowFloorSerial(long serial) {
        return index.rowFloorSerial(serial);
    }

    @Override
    public long rowCeilSerial(long serial) {
        return index.rowCeilSerial(serial);
    }

    @Override
    public long bufferRead(long offset, long length, ByteBuffer bb) {
        return pt.read(offset + bom, length, bb);
    }

    @Override
    public void bufferRead(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback) {
        pt.read(offset + bom, limitLength, traverseCallback);
    }

}
