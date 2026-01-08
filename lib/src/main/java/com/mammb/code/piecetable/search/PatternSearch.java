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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code PatternSearch} class provides an implementation of the {@link Search} interface
 * using pattern matching to find occurrences of a specified pattern within a given
 * {@link SearchSource}. It is capable of performing forward and backward searches,
 * as well as searching within a specific range.
 * @author Naotsugu Kobayashi
 */
class PatternSearch implements Search {

    /** The default chunk size. */
    static final int DEFAULT_CHUNK_SIZE = 1024 * 256;

    /** The serial document. */
    private final SearchSource source;

    /** The match flags. */
    private final int matchFlags;

    /** The ByteBuffer pool. */
    private final ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

    /**
     * Constructor.
     * @param source the source
     * @param matchFlags the match flags
     */
    PatternSearch(SearchSource source, int matchFlags) {
        this.source = source;
        this.matchFlags = matchFlags;
    }

    @Override
    public List<Found> all(CharSequence cs, int fromRow, int fromCol, int toRow, int toCol) {

        if (cs == null || cs.isEmpty()) return List.of();

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        return Chunk.of(source, fromRow, fromCol, toRow, toCol, DEFAULT_CHUNK_SIZE).stream()
            .map(c -> search(c, pattern))
            .map(FoundsInChunk::founds)
            .flatMap(Collection::stream)
            .toList();
    }

    @Override
    public Optional<Found> nextOne(CharSequence cs, int fromRow, int fromCol) {

        if (cs == null || cs.isEmpty()) return Optional.empty();

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        return Chunk.of(source, fromRow, fromCol, DEFAULT_CHUNK_SIZE).stream()
            .parallel()
            .map(c -> search(c, pattern))
            .filter(FoundsInChunk::hasFounds)
            .findFirst()
            .map(FoundsInChunk::founds)
            .map(List::getFirst);
    }

    @Override
    public Optional<Found> previousOne(CharSequence cs, int fromRow, int fromCol) {

        if (cs == null || cs.isEmpty()) return Optional.empty();

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        return Chunk.backwardOf(source, fromRow, fromCol, DEFAULT_CHUNK_SIZE).stream()
            .parallel()
            .map(c -> search(c, pattern))
            .filter(FoundsInChunk::hasFounds)
            .findFirst()
            .map(FoundsInChunk::founds)
            .map(List::getLast);
    }

    @Override
    public void forward(CharSequence cs, int fromRow, int fromCol, Consumer<FoundsInChunk> listener) {

        if (cs == null || cs.isEmpty()) return;

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        Chunk.of(source, fromRow, fromCol, DEFAULT_CHUNK_SIZE).stream()
            .parallel()
            .map(c -> search(c, pattern))
            .forEachOrdered(listener);
    }

    @Override
    public void backward(CharSequence cs, int fromRow, int fromCol, Consumer<FoundsInChunk> listener) {

        if (cs == null || cs.isEmpty()) return;

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        Chunk.backwardOf(source, fromRow, fromCol, DEFAULT_CHUNK_SIZE).stream()
            .parallel()
            .map(c -> search(c, pattern))
            .map(FoundsInChunk::reverse)
            .forEachOrdered(listener);

    }

    private FoundsInChunk search(Chunk chunk, Pattern pattern) {

        if (Thread.interrupted()) throw new RuntimeException("interrupted");

        final List<Found> founds = new ArrayList<>();
        final Charset charset = source.charset();

        ByteBuffer bb = pool.poll();

        if (bb == null) {
            bb = ByteBuffer.allocateDirect(
                Math.max(DEFAULT_CHUNK_SIZE, Math.toIntExact(chunk.length())));
        } else if (bb.remaining() < chunk.length()) {
            pool.offer(bb);
            bb = ByteBuffer.allocateDirect(Math.toIntExact(chunk.length()));
        }

        synchronized (this) {
            source.bufferRead(chunk.from(), chunk.length(), bb);
        }
        bb.flip();
        CharBuffer cb = charset.decode(bb);
        bb.clear();
        pool.offer(bb);

        int n = 0;
        int pos = 0;
        final Matcher matcher = pattern.matcher(cb);
        while (matcher.find()) {
            if (Thread.interrupted()) throw new RuntimeException("interrupted");
            pos += charset.encode(cb.slice(n, matcher.start() - n)).limit();
            n = matcher.start();
            var str = matcher.group();
            var found = new Found(chunk.from() + pos, str.length(), str.getBytes(charset).length);
            founds.add(found);
        }

        return new FoundsInChunk(founds, chunk);
    }

}
