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
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.CharsetMatch;
import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.Found;
import com.mammb.code.piecetable.PieceTable;
import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.RowEnding;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The document implementation.
 * @author Naotsugu Kobayashi
 */
public class DocumentImpl implements Document {

    /** The {@link PieceTable}. */
    private final PieceTable pt;

    /** The {@link Path} of Document. */
    private final Path path;

    /** The {@link Charset} of Document. */
    private final Charset charset;

    /** The {@link RowIndex}. */
    private final RowIndex index;

    /** The byte order mark. */
    private final byte[] bom;

    /** The row ending. */
    private final RowEnding rowEnding;

    /** Readonly or not. */
    private boolean readonly;


    /**
     * Constructor.
     * @param pt the {@link PieceTable}
     * @param path the {@link Path} of document
     * @param reader the {@link Reader}
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
     * @param progressListener the traverse callback of the document
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path, ProgressListener<byte[]> progressListener) {
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
    public void insert(int row, int rawCol, byte[] bytes) {
        if (readonly) return;
        rawCol += (row == 0) ? bom.length : 0;
        pt.insert(index.serial(row, rawCol), bytes);
        index.insert(row, rawCol, bytes);
    }


    @Override
    public void delete(int row, int rawCol, int rawLen) {
        if (readonly) return;
        rawCol += (row == 0) ? bom.length : 0;
        pt.delete(index.serial(row, rawCol), rawLen);
        index.delete(row, rawCol, rawLen);
    }


    @Override
    public byte[] get(int row, int rawCol, int rawLen) {
        rawCol += (row == 0) ? bom.length : 0;
        return pt.get(index.serial(row, rawCol), rawLen);
    }


    @Override
    public byte[] get(int row) {
        long col = index.get(row);
        col += (row == 0) ? bom.length : 0;
        int len = Math.toIntExact(index.get(row + 1) - col);
        return pt.get(col, len);
    }

    @Override
    public CharSequence getText(int row, int rawCol, int rawLen) {
        return new String(get(row, rawCol, rawLen), charset);
    }

    @Override
    public List<Found> findAll(CharSequence cs) {
        return search(cs, 0, 0, Short.MAX_VALUE);
    }

    @Override
    public Optional<Found> findNext(CharSequence cs, int row, int col) {
        col = getText(row).toString().substring(0, col).getBytes().length;
        return search(cs, row, col, Short.MAX_VALUE).stream().findFirst();
    }

    @Override
    public void readonly(boolean readonly) {
        this.readonly = readonly;
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
        return index.serial(row, col);
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
    }

    private List<Found> search(CharSequence cs, int fromRow, int fromCol, int maxFound) {

        List<Found> founds = new ArrayList<>();
        byte[] str = cs.toString().getBytes(charset);
        byte first = str[0];

        for (int row = fromRow; row < rows(); row++) {
            byte[] value = get(row);
            int max = value.length - str.length;
            int colOffset = (row == fromRow) ? fromCol : 0;
            for (int col = colOffset; col <= max; col++) {
                // look for first character
                if (value[col] != first) {
                    while (++col <= max && value[col] != first);
                }
                // found first character, now look at the rest of value
                if (col <= max) {
                    int j = col + 1;
                    int end = j + str.length - 1;
                    for (int k = 1; j < end && value[j] == str[k]; j++, k++);
                    if (j == end) {
                        // found whole charSequence
                        founds.add(new Found(row, col, str.length));
                        if (founds.size() >= maxFound) {
                            return founds;
                        }
                        col += str.length;
                    }
                }
            }
        }
        return founds;
    }

}
