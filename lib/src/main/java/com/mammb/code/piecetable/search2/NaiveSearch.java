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
import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.Progress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The naive search.
 * Bypass String instantiation by comparing them as a byte array.
 * @author Naotsugu Kobayashi
 */
public class NaiveSearch {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public NaiveSearch(Document doc) {
        this.doc = doc;
    }

    public void search(CharSequence cs, int fromRow, int fromCol,
            Progress.Listener<List<Findable.Found>> listener) {

        if (cs == null || cs.isEmpty()) return;

        Searches.withLock(doc, () ->
            buildChunks(fromRow, fromCol).stream().parallel()
                .map(c -> search(c, cs))
                .forEachOrdered(p -> Searches.notifyProgress(p, listener))
        );
    }

    private Progress<List<Findable.Found>> search(Chunk chunk, CharSequence cs) {
        byte[] pattern = cs.toString().getBytes(doc.charset());
        byte first = pattern[0];
        AtomicInteger n = new AtomicInteger(0);
        List<Findable.Found> founds = new ArrayList<>();
        doc.bufferRead(chunk.from(), chunk.length(), bb -> {
            bb.flip();
            while (bb.remaining() >= pattern.length) {
                int matchLen;
                n.getAndIncrement();
                if (first != bb.get()) continue;
                for (matchLen = 1; matchLen < pattern.length; matchLen++) {
                    n.getAndIncrement();
                    if (pattern[matchLen] != bb.get()) break;
                }
                if (matchLen == pattern.length) {
                    Pos pos = doc.pos(chunk.from() + n.get() - matchLen);
                    var found = new Findable.Found(pos.row(), pos.col(), cs.length());
                    founds.add(found);
                }
            }
            bb.compact();
            return true;
        });
        return Progress.of(chunk.to(), doc.rawSize(), founds);
    }

    private List<Chunk> buildChunks(int fromRow, int fromCol) {
        int chunkRowSize = chunkRowSize();
        List<Chunk> list = new ArrayList<>();
        long serialPrev = doc.serial(fromRow, fromCol);
        long parentFrom = serialPrev;
        long parentTo = doc.rawSize();
        for (int i = fromRow + chunkRowSize; i < doc.rows(); i += chunkRowSize) {
            long serial = doc.serial(i, 0);
            list.add(new Chunk(serialPrev, serial, parentFrom, parentTo));
            serialPrev = serial;
        }
        if (serialPrev < doc.rawSize()) {
            list.add(new Chunk(serialPrev, doc.rawSize(), parentFrom, parentTo));
        }
        return list;
    }

    private List<Chunk> buildBackwardChunks(int fromRow, int fromCol) {
        int chunkRowSize = chunkRowSize();
        List<Chunk> list = new ArrayList<>();
        long serialPrev = doc.serial(fromRow, fromCol);
        long parentFrom = serialPrev;
        for (int i = fromRow - chunkRowSize; i > 0; i -= chunkRowSize) {
            long serial = doc.serial(i, 0);
            list.add(new Chunk(serial, serialPrev, 0, parentFrom));
            serialPrev = serial;
        }
        if (serialPrev > 0) {
            list.add(new Chunk(0, serialPrev, 0, parentFrom));
        }
        return list;
    }

    private int chunkRowSize() {
        int chunkRowSize = (int) Math.ceil(
            (double) doc.rows() / Runtime.getRuntime().availableProcessors());
        return Math.max(10_000, chunkRowSize);
    }

}
