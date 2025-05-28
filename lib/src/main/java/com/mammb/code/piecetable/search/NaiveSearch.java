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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * NaiveSearch is a straightforward implementation of the Search interface.
 * It provides methods to search for patterns within a specified range
 * of documents using a specified source.
 * Bypass String instantiation by comparing them as a byte array.
 * @author Naotsugu Kobayashi
 */
class NaiveSearch implements Search {

    /** The serial document. */
    private final SearchSource source;

    /**
     * Constructor.
     * @param source the source
     */
    public NaiveSearch(SearchSource source) {
        this.source = source;
    }

    @Override
    public List<Found> all(CharSequence cs, int fromRow, int fromCol, int toRow, int toCol) {
        if (cs == null || cs.isEmpty()) return List.of();
        long from = source.serial(fromRow, fromCol);
        long to = source.serial(toRow, toCol);
        int size = Math.toIntExact(to - from);
        return Chunk.of(from, to, size).stream()
            .map(c -> search(c, cs))
            .map(FoundsInChunk::founds)
            .flatMap(Collection::stream)
            .toList();
    }

    @Override
    public Optional<Found> nextOne(CharSequence cs, int fromRow, int fromCol) {
        if (cs == null || cs.isEmpty()) return Optional.empty();
        int size = chunkSize(source.length());
        return Chunk.of(source, fromRow, fromCol, size).stream().parallel()
            .map(c -> search(c, cs))
            .filter(FoundsInChunk::hasFounds)
            .findFirst()
            .map(FoundsInChunk::founds)
            .map(List::getFirst);
    }

    @Override
    public Optional<Found> previousOne(CharSequence cs, int fromRow, int fromCol) {
        if (cs == null || cs.isEmpty()) return Optional.empty();
        int size = chunkSize(source.length());
        return Chunk.backwardOf(source, fromRow, fromCol, size).stream().parallel()
            .map(c -> search(c, cs))
            .filter(FoundsInChunk::hasFounds)
            .findFirst()
            .map(FoundsInChunk::founds)
            .map(List::getLast);
    }

    @Override
    public void forward(CharSequence cs, int fromRow, int fromCol,
            Consumer<FoundsInChunk> listener) {

        if (cs == null || cs.isEmpty()) return;
        int size = chunkSize(source.length());

        Chunk.of(source, fromRow, fromCol, size).stream().parallel()
            .map(c -> search(c, cs))
            .forEachOrdered(listener);
    }

    @Override
    public void backward(CharSequence cs, int fromRow, int fromCol,
             Consumer<FoundsInChunk> listener) {

        if (cs == null || cs.isEmpty()) return;
        int size = chunkSize(source.length());

        Chunk.backwardOf(source, fromRow, fromCol, size).stream().parallel()
            .map(c -> search(c, cs))
            .map(FoundsInChunk::reverse)
            .forEachOrdered(listener);
    }

    private FoundsInChunk search(Chunk chunk, CharSequence cs) {
        byte[] pattern = cs.toString().getBytes(source.charset());
        byte first = pattern[0];
        AtomicInteger n = new AtomicInteger(0);
        List<Found> founds = new ArrayList<>();
        source.bufferRead(chunk.from(), chunk.length(), bb -> {
            bb.flip();
            while (bb.remaining() >= pattern.length) {
                if (Thread.interrupted()) throw new RuntimeException("interrupted");
                int matchLen;
                n.getAndIncrement();
                if (first != bb.get()) continue;
                for (matchLen = 1; matchLen < pattern.length; matchLen++) {
                    n.getAndIncrement();
                    if (pattern[matchLen] != bb.get()) break;
                }
                if (matchLen == pattern.length) {
                    var found = new Found(n.get(), cs.length(), pattern.length);
                    founds.add(found);
                }
            }
            bb.compact();
            return true;
        });
        return new FoundsInChunk(founds, chunk);
    }


    private int chunkSize(long length) {
        int chunkSize = (int) Math.ceil(
            (double) length / Runtime.getRuntime().availableProcessors());
        return Math.max(1024 * 256, chunkSize);
    }

}
