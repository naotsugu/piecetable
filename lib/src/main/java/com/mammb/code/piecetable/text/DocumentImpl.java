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

import com.mammb.code.piecetable.CharsetMatch;
import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.PieceTable;
import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.RowEnding;
import com.mammb.code.piecetable.SearchContext;
import com.mammb.code.piecetable.Segment;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of the {@link Document} interface for operations on textual data.
 * This class provides methods to create, modify, query, and persist document content.
 * It is backed by a {@link PieceTable} to enable efficient text manipulations.
 * @author Naotsugu Kobayashi
 */
public class DocumentImpl implements Document {

    /** The {@link PieceTable}. */
    private final PieceTable pt;

    /** The {@link Charset} of Document. */
    private final Charset charset;

    /** The {@link RowIndex}. */
    private final RowIndex index;

    /** The byte order mark. */
    private final byte[] bom;

    /** The row ending. */
    private final RowEnding rowEnding;

    /** The offset sync. */
    private OffsetSync offsetSync;

    /** The {@link Path} of Document. */
    private Path path;

    /** Readonly or not. */
    private boolean readonly;


    /**
     * Constructor.
     * @param pt the {@link PieceTable}
     * @param path the {@link Path} of a document
     * @param reader the {@link SeqReader}
     */
    DocumentImpl(PieceTable pt, Path path, Reader reader) {
        this.pt = Objects.requireNonNull(pt);
        this.path = path;
        if (reader == null) {
            this.index = RowIndex.of();
            this.charset = StandardCharsets.UTF_8;
            this.bom = new byte[0];
            this.rowEnding = RowEnding.platform;
        } else {
            this.index = reader.index();
            this.charset = reader.charset();
            this.bom = reader.bom();
            this.rowEnding = RowEnding.estimate(reader.crCount(), reader.lfCount());
        }
    }

    /**
     * Create a new {@link Document}.
     * @return a new {@link Document}
     */
    public static DocumentImpl of() {
        return new DocumentImpl(PieceTable.of(), null, null);
    }

    /**
     * Create a new {@link Document} from the specified path.
     * @param path the {@link Path} of the document
     * @param charsetMatches the {@link CharsetMatch}
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path, CharsetMatch... charsetMatches) {
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path, charsetMatches));
    }

    /**
     * Create a Reader from the specified byte array.
     * @param bytes the specified byte array
     * @param charsetMatches the {@link CharsetMatch}
     * @return a new {@link Document}
     */
    public static DocumentImpl of(byte[] bytes, CharsetMatch... charsetMatches) {
        return new DocumentImpl(PieceTable.of(bytes), null, Reader.of(bytes, charsetMatches));
    }

    /**
     * Create a new {@link Document}.
     * @param path the {@link Path} of the document
     * @param listener the progress listener
     * @param charsetMatches the {@link CharsetMatch}
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path, Consumer<Segment> listener, CharsetMatch... charsetMatches) {
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path, listener, charsetMatches));
    }

    @Override
    public void insert(int row, int col, CharSequence cs) {
        if (readonly) return;
        insert(row, asRawCol(row, col), cs.toString().getBytes(charset));
    }

    @Override
    public void delete(int row, int col, CharSequence cs) {
        if (readonly) return;
        delete(row, asRawCol(row, col), cs.toString().getBytes(charset).length);
    }

    @Override
    public CharSequence getText(int row) {
        return charset.decode(ByteBuffer.wrap(get(row)));
    }

    @Override
    public void insert(int row, int rawCol, byte[] bytes) {
        if (readonly) return;
        long offset = index.offset(row, rawCol);
        pt.insert(offset + bom.length, bytes);
        index.insert(row, rawCol, bytes);
        if (offsetSync != null) offsetSync.insert(offset, bytes.length);
    }

    @Override
    public void delete(int row, int rawCol, int rawLen) {
        if (readonly) return;
        long offset = index.offset(row, rawCol);
        pt.delete(offset + bom.length, rawLen);
        index.delete(row, rawCol, rawLen);
        if (offsetSync != null) offsetSync.delete(offset, rawLen);
    }

    @Override
    public byte[] get(int row, int rawCol, int rawLen) {
        return pt.get(index.offset(row, rawCol) + bom.length, rawLen);
    }

    @Override
    public byte[] get(int row) {
        long serial = index.get(row);
        int len = Math.toIntExact(index.get(row + 1) - serial);
        return pt.get(serial + bom.length, len);
    }

    @Override
    public CharSequence getText(int row, int rawCol, int rawLen) {
        return charset.decode(ByteBuffer.wrap(get(row, rawCol, rawLen)));
    }

    @Override
    public void readonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public boolean readonly() {
        return readonly;
    }

    @Override
    public int rows() {
        return index.rowSize();
    }

    @Override
    public long rawSize() {
        return pt.length() - bom.length;
    }

    @Override
    public long serial(int row, int col) {
        return index.offset(row, asRawCol(row, col));
    }

    @Override
    public Pos pos(long serial) {
        int[] ret = index.pos(serial);
        var cb = charset.decode(ByteBuffer.wrap(get(ret[0], 0, ret[1])));
        return new Pos(ret[0], cb.length());
    }

    @Override
    public Charset charset() {
        return charset;
    }

    @Override
    public RowEnding rowEnding() {
        return rowEnding;
    }

    @Override
    public byte[] bom() {
        return Arrays.copyOf(bom, bom.length);
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public void save(Path path) {
        pt.save(path);
        this.path = path;
    }

    @Override
    public void write(Path path) {
        pt.write(path);
    }

    @Override
    public void close() {
        pt.close();
    }

    @Override
    public SearchContext search() {
        var source = new SearchSourceImpl(pt, index, charset, bom.length);
        var search = new SearchContextImpl(source, r -> {
            final boolean ro = readonly();
            try {
                if (!ro) readonly(true);
                r.run();
            } finally {
                if (!ro) readonly(false);
            }
        });
        offsetSync = search;
        return search;
    }

    /**
     * Get the length of bytes from the beginning of the row.
     * to the specified column number based on the given row number and column number.
     * <pre>
     *                           | a     | b     | あ       | 𠀋           |
     *                           --------------------------------------------
     *  codepoint                | 61    | 62    | 12,354   | 131,083     |
     *  UTF-8                    | 61    | 62    | E3 81 82 | f0 a0 80 8b |
     *  UTF-16                   | 00 61 | 00 62 | 30 42    | d8 40 dc 0b |
     *  ---------------------------------------------------------------------
     *  length in codepoints     0       1       2          3             4
     *  length in java(utf-16)   0       1       2          3      4      5
     *  ---------------------------------------------------------------------
     *  bytes length in UTF-8    0       1       2          5             9
     *  bytes length in UTF-16   0       2       4          6            10
     *
     *
     *                 | a | b | あ | 𠀋 |   : UTF-8
     *  asRawCol(_, 3)              ^       -> return 5
     *  asRawCol(_, 5)                  ^   -> return 9
     * </pre>
     *
     * @param row the specified row
     * @param col the specified column (character length in java, not a code points)
     * @return the length of bytes from the beginning of the row
     */
    private int asRawCol(int row, int col) {
        var cb = charset.decode(ByteBuffer.wrap(get(row)));
        var bb = charset.encode(cb.subSequence(0, Math.min(col, cb.length())));
        return bb.limit();
    }

}
