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
package com.mammb.dev.picetable;

import com.mammb.dev.picetable.index.LineIndex;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Document.
 * @author Naotsugu Kobayashi
 */
public class Document {

    private PieceTable pt;
    private Path path;
    private Charset charset;
    private LineIndex index;


    public Document(PieceTable pt, Path path) {
        this.pt = pt;
        this.path = path;
        this.charset = StandardCharsets.UTF_8;
        this.index = LineIndex.of();
        index.add(Files.readAllBytes(path));
    }


    public static Document of() {
        return new Document(PieceTable.of(), null);
    }


    public static Document of(Path path) {
        return new Document(PieceTable.of(path), path);
    }


    public void insert(int line, int col, CharSequence cs) {
        byte[] bytes = cs.toString().getBytes(charset);
        pt.insert(index.get(line) + col, bytes);
        index.insert(line, col, bytes);
    }


    public void delete(int line, int col, int len) {
        pt.delete(index.get(line) + col, len);
        index.delete(line, col, len);
    }


    public byte[] get(int line, int col, int len) {
        return pt.get(index.get(line) + col, len);
    }


    public long length() {
        return pt.length();
    }

}
