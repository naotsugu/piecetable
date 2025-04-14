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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * The document implementation.
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
     * @param path the {@link Path} of document
     * @param reader the {@link SeqReader}
     */
    DocumentImpl(PieceTable pt, Path path, Reader reader) {
        this.pt = pt;
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
     * Create a new {@link Document}.
     * @param path the {@link Path} of the document
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path) {
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path));
    }

    /**
     * Create a new {@link Document}.
     * @param path the {@link Path} of the document
     * @param progressListener the progress listener
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path, Consumer<Segment> progressListener) {
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path, progressListener));
    }

    /**
     * Create a new {@link Document}.
     * @param path the {@link Path} of the document
     * @param charset the {@link Charset} of the document
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path, Charset charset) {
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path, charset));
    }

    /**
     * Create a new {@link Document}.
     * @param path the {@link Path} of the document
     * @param charsetMatches the {@link CharsetMatch}
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path, CharsetMatch... charsetMatches) {
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path, -1, charsetMatches));
    }

    @Override
    public void insert(int row, int col, CharSequence cs) {
        if (readonly) return;
        col = getText(row).toString().substring(0, col).getBytes(charset).length;
        insert(row, col, cs.toString().getBytes(charset));
    }

    @Override
    public void delete(int row, int col, CharSequence cs) {
        if (readonly) return;
        col = getText(row).toString().substring(0, col).getBytes(charset).length;
        delete(row, col, cs.toString().getBytes(charset).length);
    }

    @Override
    public CharSequence getText(int row) {
        return new String(get(row), charset);
    }

    @Override
    public void insert(int row, int col, byte[] bytes) {
        if (readonly) return;
        long offset = index.offset(row, col);
        pt.insert(offset + bom.length, bytes);
        index.insert(row, col, bytes);
        if (offsetSync != null) offsetSync.insert(offset, bytes.length);
    }

    @Override
    public void delete(int row, int col, int rawLen) {
        if (readonly) return;
        long offset = index.offset(row, col);
        pt.delete(offset + bom.length, rawLen);
        index.delete(row, col, rawLen);
        if (offsetSync != null) offsetSync.delete(offset, rawLen);
    }

    @Override
    public byte[] get(int row, int col, int rawLen) {
        return pt.get(index.offset(row, col) + bom.length, rawLen);
    }

    @Override
    public byte[] get(int row) {
        long serial = index.get(row);
        int len = Math.toIntExact(index.get(row + 1) - serial);
        return pt.get(serial + bom.length, len);
    }

    @Override
    public CharSequence getText(int row, int rawCol, int rawLen) {
        return new String(get(row, rawCol, rawLen), charset);
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
        return index.offset(row, col);
    }

    @Override
    public Pos pos(long serial) {
        int[] ret = index.pos(serial);
        return new Pos(ret[0], ret[1]);
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

}
