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
package com.mammb.code.editor2.model;

import com.mammb.code.piecetable.PieceTable;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * BasicContent.
 * @author Naotsugu Kobayashi
 */
public class BasicContent implements Content {

    /** The piece table. */
    private PieceTable pt;

    /** The content path. */
    private Path path;

    /**
     * Constructor.
     */
    public BasicContent() {
        this.pt = PieceTable.of("");
    }

    @Override
    public int length() {
        return pt.length();
    }

    @Override
    public byte[] bytes(int startPos, Predicate<byte[]> until) {
        return pt.bytes(startPos, until);
    }

    @Override
    public byte[] bytesBefore(int startPos, Predicate<byte[]> until) {
        return pt.bytesBefore(startPos, until);
    }

    @Override
    public void handle(Edit event) {
        if (event instanceof Edit.InsertEdit insert) {
            pt.insert(insert.pos(), insert.cs());
        } else if (event instanceof Edit.DeleteEdit delete) {
            pt.delete(delete.pos(), delete.cs().length());
        }
    }

}
