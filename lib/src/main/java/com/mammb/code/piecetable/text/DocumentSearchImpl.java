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
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.DocumentSearch;
import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.PosLen;
import com.mammb.code.piecetable.Segment;
import com.mammb.code.piecetable.search.FoundsInChunk;
import com.mammb.code.piecetable.search.Search;
import com.mammb.code.piecetable.search.SearchSource;
import java.util.List;
import java.util.function.Consumer;

/**
 * The document search implementation.
 * @author Naotsugu Kobayashi
 */
public class DocumentSearchImpl implements DocumentSearch {

    /** The search source. */
    private final SearchSource source;
    /** The around runnable. */
    private final Consumer<Runnable> aroundRun;
    /** The search. */
    private Search search;

    /**
     * Constructor.
     * @param source the search source
     * @param aroundRun the around runnable
     */
    DocumentSearchImpl(SearchSource source, Consumer<Runnable> aroundRun) {
        this.source = source;
        this.aroundRun = aroundRun;
    }

    @Override
    public void all(Spec spec, Pos pos, Consumer<Segment.Valued<List<PosLen>>> listener) {
        search = build(source, spec.patternCase());
        aroundRun.accept(() -> {
            switch (spec.direction()) {
                case FORWARD ->
                    search.search(spec.pattern(), pos.row(), pos.col(), foundsInChunk ->
                        listener.accept(Segment.valuedOf(
                            foundsInChunk.chunk().length(),
                            foundsInChunk.chunk().parentLength(),
                            toPosLen(foundsInChunk)))
                    );
                case BACKWARD ->
                    search.searchBackward(spec.pattern(), pos.row(), pos.col(), foundsInChunk ->
                        listener.accept(Segment.valuedOf(
                            foundsInChunk.chunk().length(),
                            foundsInChunk.chunk().parentLength(),
                            toPosLen(foundsInChunk)))
                    );
            }
        });
    }

    private List<PosLen> toPosLen(FoundsInChunk foundsInChunk) {
        return foundsInChunk.founds().stream()
            .map(f -> {
                var p = source.pos(f.offset());
                return new PosLen(p[0], p[1], f.len());
            }).toList();
    }

    private Search build(SearchSource source, PatternCase patternCase) {
        return switch (patternCase) {
            case LITERAL -> Search.of(source);
            case CASE_INSENSITIVE -> Search.caseInsensitiveOf(source);
            case REGEX -> Search.regexOf(source);
        };
    }

}
