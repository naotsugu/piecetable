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
package com.mammb.code.piecetable.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * The naive search.
 * Bypass String instantiation by comparing them as a byte array.
 * @author Naotsugu Kobayashi
 */
public class NaiveSearch implements Search {

    /** The serial document. */
    private final SearchSource source;
    /** cancel?. */
    private volatile boolean cancel = false;

    /**
     * Constructor.
     * @param source the source
     */
    public NaiveSearch(SearchSource source) {
        this.source = source;
    }

    @Override
    public void search(CharSequence cs, int fromRow, int fromCol,
            Consumer<FoundsInChunk> listener) {
        if (cs == null || cs.isEmpty()) return;
        int size = chunkSize();
        Chunk.of(source, fromRow, fromCol, size).stream().parallel()
            .map(c -> search(c, cs))
            .forEachOrdered(c -> {
                if (!cancel) listener.accept(c);
            });
    }

    @Override
    public void searchBackward(CharSequence cs, int fromRow, int fromCol,
            Consumer<FoundsInChunk> listener) {

        if (cs == null || cs.isEmpty()) return;
        int size = chunkSize();
        Chunk.backwardOf(source, fromRow, fromCol, size).stream().parallel()
            .map(c -> search(c, cs))
            .map(FoundsInChunk::reverse)
            .forEachOrdered(c -> {
                if (!cancel) listener.accept(c);
            });
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private FoundsInChunk search(Chunk chunk, CharSequence cs) {
        byte[] pattern = cs.toString().getBytes(source.charset());
        byte first = pattern[0];
        AtomicInteger n = new AtomicInteger(0);
        List<Found> founds = new ArrayList<>();
        source.bufferRead(chunk.from(), chunk.length(), bb -> {
            bb.flip();
            while (bb.remaining() >= pattern.length) {
                int matchLen;
                n.getAndIncrement();
                if (first != bb.get()) continue;
                for (matchLen = 1; matchLen < pattern.length; matchLen++) {
                    n.getAndIncrement();
                    if (pattern[matchLen] != bb.get() || cancel) break;
                }
                if (matchLen == pattern.length) {
                    var found = new Found(n.get(), cs.length());
                    founds.add(found);
                }
            }
            bb.compact();
            return !cancel;
        });
        return new FoundsInChunk(founds, chunk);
    }


    private int chunkSize() {
        int chunkSize = (int) Math.ceil(
            (double) source.length() / Runtime.getRuntime().availableProcessors());
        return Math.max(1024 * 256, chunkSize);
    }

}
