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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pattern search.
 * @author Naotsugu Kobayashi
 */
public class PatternSearch implements Search {

    /** The default chunk size. */
    static final int DEFAULT_CHUNK_SIZE = 1024 * 256;

    /** The serial document. */
    private final SerialSource source;

    /** The match flags. */
    private final int matchFlags;

    /** The ByteBuffer pool. */
    private final ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

    /**
     * Constructor.
     * @param source the source
     */
    PatternSearch(SerialSource source, int matchFlags) {
        this.source = source;
        this.matchFlags = matchFlags;
    }

    @Override
    public void search(CharSequence cs, int fromRow, int fromCol,
            Function<FoundsInChunk, Boolean> listener) {

        if (cs == null || cs.isEmpty()) return;

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        Chunk.of(source, fromRow, fromCol, DEFAULT_CHUNK_SIZE).stream()
            .parallel()
            .map(c -> search(c, pattern))
            .forEachOrdered(fic -> notifyProgress(fic, listener));
    }

    @Override
    public void searchBackward(CharSequence cs, int fromRow, int fromCol,
            Function<FoundsInChunk, Boolean> listener) {

        if (cs == null || cs.isEmpty()) return;

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        Chunk.backwardOf(source, fromRow, fromCol, DEFAULT_CHUNK_SIZE).stream()
            .parallel()
            .map(c -> search(c, pattern))
            .map(FoundsInChunk::reverse)
            .forEachOrdered(fic -> notifyProgress(fic, listener));

    }

    private FoundsInChunk search(Chunk chunk, Pattern pattern) {

        List<Found> founds = new ArrayList<>();

        ByteBuffer bb = pool.poll();
        if (bb == null) {
            bb = ByteBuffer.allocateDirect(DEFAULT_CHUNK_SIZE);
        }

        source.bufferRead(chunk.from(), chunk.length(), bb);
        bb.flip();
        CharBuffer cb = source.charset().decode(bb);
        bb.clear();
        pool.offer(bb);

        int n = 0;
        int pos = 0;
        Matcher matcher = pattern.matcher(cb);
        while (matcher.find()) {
            pos += source.charset().encode(cb.slice(n, matcher.start() - n)).limit();
            n = matcher.start();
            var found = new Found(chunk.from() + pos, matcher.end() - matcher.start());
            founds.add(found);
        }

        return new FoundsInChunk(founds, chunk);
    }

    void notifyProgress(FoundsInChunk foundsInChunk,
            Function<FoundsInChunk, Boolean> listener) {
        boolean continuation = listener.apply(foundsInChunk);
        if (!continuation) throw new RuntimeException("interrupted.");
    }

}
