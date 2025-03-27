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
package com.mammb.code.piecetable.search2;

import com.mammb.code.piecetable.Document;
import java.util.ArrayList;
import java.util.List;

/**
 * The AbstractSearch.
 * @author Naotsugu Kobayashi
 */
public abstract class AbstractSearch {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    AbstractSearch(Document doc) {
        this.doc = doc;
    }

    List<Chunk> buildChunk(int fromRow, int fromCol, int size) {
        List<Chunk> chunks = new ArrayList<>();
        long from = doc.serial(fromRow, fromCol);
        while (true) {
            long to = doc.rowFloorSerial(from + size);
            chunks.add(new Chunk(from, to));
            if (to >= doc.rawSize()) break;
            from = to;
        }
        return chunks;
    }

    List<Chunk> buildBackwardChunk(int fromRow, int fromCol, int size) {
        List<Chunk> chunks = new ArrayList<>();
        long from = doc.serial(fromRow, fromCol);
        while (true) {
            long to = doc.rowCeilSerial(from - size);
            chunks.add(new Chunk(from, to));
            if (to == 0) break;
            from = to;
        }
        return chunks;
    }

}
