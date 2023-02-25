/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.model.specifics;

import com.mammb.code.editor3.lang.Strings;
import com.mammb.code.editor3.model.Content;
import com.mammb.code.editor3.model.Edit;
import com.mammb.code.piecetable.PieceTable;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Content.
 * @author Naotsugu Kobayashi
 */
public class ContentImpl implements Content {

    /** The piece table. */
    private PieceTable pt;

    /** The content path. */
    private Path path;

    /** The number of row. */
    private int rowSize;


    /**
     * Constructor.
     */
    public ContentImpl() {
        this.pt = PieceTable.of("");
        this.rowSize = 0;
    }

    /**
     * Create content for specified path.
     * @param path the content path
     */
    public ContentImpl(Path path) {
        this.pt = PieceTable.of(path);
        this.path = path;
        this.rowSize = 0;
        this.rowSize = pt.count(bytes -> bytes[0] == '\n');
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public int length() {
        return pt.length();
    }

    @Override
    public int rowSize() { return rowSize; }

    @Override
    public byte[] bytes(int startPos, Predicate<byte[]> until) {
        return pt.bytes(startPos, until);
    }

    @Override
    public int count(int startPos, Predicate<byte[]> until) {
        return pt.count(startPos, until);
    }

    @Override
    public byte[] bytesBefore(int startPos, Predicate<byte[]> until) {
        return pt.bytesBefore(startPos, until);
    }

    @Override
    public void handle(Edit edit) {
        if (edit.isInsert()) {
            pt.insert(edit.position(), edit.string());
            rowSize += Strings.countLf(edit.string());
        } else {
            pt.delete(edit.position(), Strings.codePointCount(edit.string()));
            rowSize -= Strings.countLf(edit.string());
        }
    }

}
