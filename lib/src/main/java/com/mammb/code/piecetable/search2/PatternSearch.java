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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pattern search.
 * @author Naotsugu Kobayashi
 */
public abstract class PatternSearch {

    /** The source document. */
    private final Document doc;

    /** The chunk size. */
    private final int CHUNK_SIZE = 1024 * 256;

    /** The ByteBuffer pool. */
    private final ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

    /**
     * Constructor.
     * @param doc the source document
     */
    PatternSearch(Document doc) {
        this.doc = doc;
    }

    void search(CharSequence cs, int matchFlags, int fromRow, int fromCol,
            Progress.Listener<List<Findable.Found>> listener) {

        if (cs == null || cs.isEmpty()) return;

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        Searches.withLock(doc, () ->
            Searches.chunks(doc, fromRow, fromCol, CHUNK_SIZE).stream()
                .parallel()
                .map(c -> search(c, pattern))
                .map(m -> Progress.of(m.chunk.distance(), m.chunk.parentLength(), m.founds))
                .forEachOrdered(m -> Searches.notifyProgress(m, listener))
        );
    }

    void searchBackward(CharSequence cs, int matchFlags, int fromRow, int fromCol,
            Progress.Listener<List<Findable.Found>> listener) {

        if (cs == null || cs.isEmpty()) return;

        Pattern pattern = Pattern.compile(cs.toString(), matchFlags);

        Searches.withLock(doc, () ->
            Searches.backwardChunks(doc, fromRow, fromCol, CHUNK_SIZE).stream()
                .parallel()
                .map(c -> search(c, pattern))
                .map(m -> Progress.of(m.chunk.reverseDistance(), m.chunk.parentLength(), Searches.reverse(m.founds)))
                .forEachOrdered(m -> Searches.notifyProgress(m, listener))
        );
    }

    private Match search(Chunk chunk, Pattern pattern) {

        List<Findable.Found> founds = new ArrayList<>();

        ByteBuffer bb = pool.poll();
        if (bb == null) {
            bb = ByteBuffer.allocateDirect(CHUNK_SIZE);
        }

        doc.bufferRead(chunk.from(), chunk.length(), bb);
        bb.flip();
        CharBuffer cb = doc.charset().decode(bb);
        bb.clear();
        pool.offer(bb);

        Matcher matcher = pattern.matcher(cb);
        while (matcher.find()) {
            Pos pos = doc.pos(chunk.from() + matcher.start());
            var found = new Findable.Found(pos.row(), pos.col(), matcher.end() - matcher.start());
            founds.add(found);
        }

        return new Match(chunk, founds);
    }

    private record Match(Chunk chunk, List<Findable.Found> founds) { }

}
