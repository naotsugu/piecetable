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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The document implementation.
 * @author Naotsugu Kobayashi
 */
public class DocumentImpl implements Document {

    /** The {@link PieceTable}. */
    private PieceTable pt;

    /** The {@link Path} of Document. */
    private Path path;

    /** The {@link Charset} of Document. */
    private Charset charset;

    /** The {@link RowIndex}. */
    private RowIndex index;

    /** The byte order mark. */
    private byte[] bom;


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
        } else {
            this.index = reader.index();
            this.charset = reader.charset();
            this.bom = reader.bom();
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
        return new DocumentImpl(PieceTable.of(path), path, Reader.of(path,charsetMatches));
    }


    @Override
    public void insert(int row, int col, CharSequence cs) {
        insert(row, col, cs.toString().getBytes(charset));
    }


    @Override
    public void insert(int row, int col, byte[] bytes) {
        col += (row == 0) ? bom.length : 0;
        pt.insert(index.get(row) + col, bytes);
        index.insert(row, col, bytes);
    }


    @Override
    public void delete(int row, int col, int len) {
        col += (row == 0) ? bom.length : 0;
        pt.delete(index.get(row) + col, len);
        index.delete(row, col, len);
    }


    @Override
    public byte[] get(int row, int col, int len) {
        col += (row == 0) ? bom.length : 0;
        return pt.get(index.get(row) + col, len);
    }


    @Override
    public byte[] get(int row) {
        long col = index.get(row);
        col += (row == 0) ? bom.length : 0;
        int len = Math.toIntExact(index.get(row + 1) - col);
        return pt.get(col, len);
    }


    @Override
    public CharSequence getText(int row, int col, int len) {
        return new String(get(row, col, len), charset);
    }


    @Override
    public CharSequence getText(int row) {
        return new String(get(row), charset);
    }

    @Override
    public List<Found> findAll(CharSequence cs) {
        return search(cs, 0, 0, Short.MAX_VALUE);
    }

    @Override
    public Optional<Found> findNext(CharSequence cs, int row, int col) {
        return search(cs, 0, 0, Short.MAX_VALUE).stream().findFirst();
    }

    @Override
    public int rows() {
        return index.rowSize();
    }


    @Override
    public long length() {
        return pt.length() - bom.length;
    }


    @Override
    public Charset charset() {
        return charset;
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
