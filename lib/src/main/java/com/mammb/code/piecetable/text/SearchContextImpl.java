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
import com.mammb.code.piecetable.SearchContext;
import com.mammb.code.piecetable.Segment;
import com.mammb.code.piecetable.search.Found;
import com.mammb.code.piecetable.search.FoundsInChunk;
import com.mammb.code.piecetable.search.Search;
import com.mammb.code.piecetable.search.SearchSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The search context implementation.
 * @author Naotsugu Kobayashi
 */
public class SearchContextImpl implements SearchContext {

    /** The search source. */
    private final SearchSource source;
    /** The around runnable. */
    private final Consumer<Runnable> aroundRun;

    /** The current find spec. */
    private Spec currentSpec;
    /** The founds. */
    private List<Found> founds = new ArrayList<>();


    public SearchContextImpl(SearchSource source, Consumer<Runnable> aroundRun) {
        this.source = source;
        this.aroundRun = aroundRun;
    }

    public void findAll(Spec spec, Consumer<Segment.Valued<List<PosLen>>> consumer) {
        Search s = build(source, spec.patternCase());
        aroundRun.accept(() -> s.forward(spec.pattern(), 0, 0, accept(consumer)));
        currentSpec = spec;
    }

    public Optional<PosLen> findNext(Pos pos) {
        long offset = source.offset(pos.row(), pos.col());
        return founds.stream().filter(f -> f.offset() >= offset).findFirst().map(this::toPosLen);
    }

    public Optional<PosLen> findPrevious(Pos pos) {
        long offset = source.offset(pos.row(), pos.col());
        // TODO f.offset() + f.len()
        return founds.stream().filter(f -> f.offset() + f.len() < offset).reduce((_, s) -> s).map(this::toPosLen);
    }

    public Optional<PosLen> findNext(Spec spec, Pos pos) {
        Search s = build(source, spec.patternCase());
        List<PosLen> list = new ArrayList<>();
        aroundRun.accept(() -> {
            Optional<Found> found = switch (spec.direction()) {
                case FORWARD -> s.nextOne(spec.pattern(), pos.row(), pos.col());
                case BACKWARD -> s.previousOne(spec.pattern(), pos.row(), pos.col());
            };
            found.map(this::toPosLen).ifPresent(list::add);
        });
        return list.stream().findFirst();
    }

    public void clear() {
        currentSpec = null;
        founds.clear();
    }

    void insert(int row, int col, int rawLen) {
        long offset = source.offset(row, col);
        for (int i = 0; i < founds.size(); i++) {
            Found f = founds.get(i);
            // TODO f.offset() + f.len()
            if (f.offset() + f.len() <= offset) {
                continue;
            }
            if (f.offset() <= offset && offset < f.offset() + f.len()) {
                founds.remove(i);
            } else {
                founds.set(i, new Found(f.offset() + rawLen, f.len()));
            }
        }
    }

    void delete(int row, int col, int rawLen) {
        long offset = source.offset(row, col);
        for (int i = 0; i < founds.size(); i++) {
            Found f = founds.get(i);
            // TODO f.offset() + f.len()
            if (f.offset() + f.len() <= offset) {
                continue;
            }
            if (f.offset() <= offset && offset < f.offset() + f.len()) {
                founds.remove(i);
            } else {
                founds.set(i, new Found(f.offset() - rawLen, f.len()));
            }
        }
    }

    private Search build(SearchSource source, PatternCase patternCase) {
        return switch (patternCase) {
            case LITERAL -> Search.of(source);
            case CASE_INSENSITIVE -> Search.caseInsensitiveOf(source);
            case REGEX -> Search.regexOf(source);
        };
    }

    private Consumer<FoundsInChunk> accept(Consumer<Segment.Valued<List<PosLen>>> consumer) {
        return foundsInChunk -> {
            founds.addAll(foundsInChunk.founds());
            var seg = Segment.valuedOf(
                foundsInChunk.chunk().length(),
                foundsInChunk.chunk().parentLength(),
                toPosLen(foundsInChunk));
            consumer.accept(seg);
        };
    }

    private List<PosLen> toPosLen(FoundsInChunk foundsInChunk) {
        return foundsInChunk.founds().stream()
            .map(this::toPosLen)
            .toList();
    }

    private PosLen toPosLen(Found found) {
        var p = source.pos(found.offset());
        return new PosLen(p[0], p[1], found.len());
    }

}
