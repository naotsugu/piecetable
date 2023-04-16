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
package com.mammb.code.editor3.model;

import com.mammb.code.editor3.lang.Strings;
import com.mammb.code.piecetable.PieceTable;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Content.
 *
 * <pre>
 *              0          0   1          0   1   2           0   1   2   3
 *            |  |       | a | b |      | a | b | $ |       | a | b | $ | c |
 *
 *            1:         1 : ab         1 : ab$             1 : ab$
 *                                      2 :                 2 : c
 *           ----------------------------------------------------------------
 *  rowSize : 1          1              2                   2
 * </pre>
 * @author Naotsugu Kobayashi
 */
public class ContentImpl implements Content {

    /** The piece table. */
    private final PieceTable pt;

    /** The content path. */
    private Path path;

    /** The number of row. */
    private int rowSize;


    /**
     * Constructor.
     */
    public ContentImpl() {
        this.pt = PieceTable.of("");
        this.rowSize = 1;
    }

    /**
     * Create content for specified path.
     * @param path the content path
     */
    public ContentImpl(Path path) {
        this.pt = PieceTable.of(path);
        this.path = path;
        this.rowSize = pt.count(bytes -> bytes[0] == '\n') + 1;
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
    public void save() {
        pt.write(path);
    }


    @Override
    public void saveAs(Path path) {
        pt.write(path);
        this.path = path;
    }


    @Override
    public void handle(Edit edit) {
        if (edit.isInsert()) {
            pt.insert(edit.codePointPosition(), edit.string());
            rowSize += Strings.countLf(edit.string());
        } else if (edit.isDelete()) {
            pt.delete(edit.codePointPosition(), Strings.codePointCount(edit.string()));
            rowSize -= Strings.countLf(edit.string());
        }
    }

}
