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
import com.mammb.code.piecetable.Findable;
import com.mammb.code.piecetable.Progress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The search utilities.
 * @author Naotsugu Kobayashi
 */
final class Searches {

    /** The default chunk size. */
    static final int DEFAULT_CHUNK_SIZE = 1024 * 256;

    static List<Chunk> chunks(Document doc, int fromRow, int fromCol, int size) {

        List<Chunk> chunks = new ArrayList<>();
        long from = doc.serial(fromRow, fromCol);
        long parentFrom = from;
        long parentTo = doc.rawSize();
        while (true) {
            long to = doc.rowFloorSerial(from + size);
            chunks.add(new Chunk(from, to, parentFrom, parentTo));
            if (to >= parentTo) break;
            from = to;
        }
        return chunks;
    }

    static List<Chunk> backwardChunks(Document doc, int fromRow, int fromCol, int size) {
        List<Chunk> chunks = new ArrayList<>();
        long from = doc.serial(fromRow, fromCol);
        long parentFrom = from;
        while (true) {
            long to = doc.rowCeilSerial(from - size);
            chunks.add(new Chunk(to, from, 0, parentFrom));
            if (to == 0) break;
            from = to;
        }
        return chunks;
    }

    static void withLock(Document doc, Runnable r) {
        final boolean ro = doc.readonly();
        try {
            if (!ro) doc.readonly(true);
            r.run();
        } finally {
            if (!ro) doc.readonly(false);
        }
    }

    static void notifyProgress(Progress<List<Findable.Found>> progress,
            Progress.Listener<List<Findable.Found>> listener) {
        boolean continuation = listener.accept(progress);
        if (!continuation) throw new RuntimeException("interrupted.");
    }

    static List<Findable.Found> reverse(List<Findable.Found> founds) {
        Collections.reverse(founds);
        return founds;
    }

}
