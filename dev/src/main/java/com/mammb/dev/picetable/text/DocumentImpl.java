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
package com.mammb.dev.picetable.text;

import com.mammb.dev.picetable.Document;
import com.mammb.dev.picetable.PieceTable;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * The document implementation.
 * @author Naotsugu Kobayashi
 */
public class DocumentImpl implements Document {

    private PieceTable pt;
    private Path path;
    private Charset charset;
    private RowIndex index;

    public DocumentImpl(PieceTable pt, Path path) {
        this.pt = pt;
        this.path = path;
        var reader = Reader.of(path);
        this.index = reader.index();
        this.charset = reader.charset();
    }

    public static DocumentImpl of() {
        return new DocumentImpl(PieceTable.of(), null);
    }


    public static DocumentImpl of(Path path) {
        return new DocumentImpl(PieceTable.of(path), path);
    }

    @Override
    public void insert(int row, int col, CharSequence cs) {
        insert(row, col, cs.toString().getBytes(charset));
    }

    @Override
    public void insert(int row, int col, byte[] bytes) {
        pt.insert(index.get(row) + col, bytes);
        index.insert(row, col, bytes);
    }

    @Override
    public void delete(int row, int col, int len) {
        pt.delete(index.get(row) + col, len);
        index.delete(row, col, len);
    }

    @Override
    public byte[] get(int row, int col, int len) {
        return pt.get(index.get(row) + col, len);
    }

    @Override
    public byte[] get(int row) {
        long pos = index.get(row);
        int len = Math.toIntExact(index.get(row + 1) - pos);
        return pt.get(index.get(row), len);
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
        return pt.length();
    }

}
