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

import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.PieceTable;
import java.nio.charset.Charset;
import java.nio.file.Path;

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
     */
    private DocumentImpl(PieceTable pt, Path path) {
        this.pt = pt;
        this.path = path;
        var reader = Reader.of(path);
        this.index = reader.index();
        this.charset = reader.charset();
        this.bom = reader.bom();
    }


    /**
     * Create a new {@link Document}.
     * @return a new {@link Document}
     */
    public static DocumentImpl of() {
        return new DocumentImpl(PieceTable.of(), null);
    }


    /**
     * Create a new {@link Document}.
     * @param path the {@link Path} of document
     * @return a new {@link Document}
     */
    public static DocumentImpl of(Path path) {
        return new DocumentImpl(PieceTable.of(path), path);
    }


    @Override
    public void insert(int row, int col, CharSequence cs) {
        col += (row == 0) ? bom.length : 0;
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
    public long length() {
        return pt.length() - bom.length;
    }

}
