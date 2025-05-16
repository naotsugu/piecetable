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

import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.PosLen;
import com.mammb.code.piecetable.SearchContext;
import com.mammb.code.piecetable.Segment;
import com.mammb.code.piecetable.search.Found;
import com.mammb.code.piecetable.search.FoundsInChunk;
import com.mammb.code.piecetable.search.Search;
import com.mammb.code.piecetable.search.SearchSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Implementation of the {@link SearchContext} and {@link OffsetSync} interfaces.
 * Provides search context management, search operations, and offset synchronization
 * for handling text-based searches and modifications.
 * @author Naotsugu Kobayashi
 */
public class SearchContextImpl implements SearchContext, OffsetSync {

    /** The search source. */
    private final SearchSource source;
    /** The around runnable. */
    private final Consumer<Runnable> aroundRun;
    /** The founds. */
    private final List<Found> founds = new ArrayList<>();

    /**
     * Constructor.
     * @param source the search source
     * @param aroundRun the around runnable
     */
    SearchContextImpl(SearchSource source, Consumer<Runnable> aroundRun) {
        this.source = source;
        this.aroundRun = aroundRun;
    }

    @Override
    public void findAll(Spec spec, Consumer<Segment.Valued<List<PosLen>>> consumer) {
        Search s = build(source, spec.patternCase());
        aroundRun.accept(() -> s.forward(spec.pattern(), 0, 0, accept(consumer)));
        Collections.sort(founds);
    }

    @Override
    public Optional<PosLen> next(Pos pos, Direction direction) {

        if (founds.isEmpty())
            return Optional.empty();

        long offset = source.offset(pos.row(), pos.col());
        int index = Collections.binarySearch(founds, new Found(offset, 0, 0));
        index = (index >= 0) ? index : ~index;
        return switch (direction) {
            case FORWARD -> (index < founds.size()) ? Optional.of(toPosLen(founds.get(index))) : Optional.empty();
            case BACKWARD -> (index > 0) ? Optional.of(toPosLen(founds.get(index - 1))) : Optional.empty();
        };
    }

    @Override
    public Optional<PosLen> findOne(Spec spec, Pos pos, Direction direction) {
        Search s = build(source, spec.patternCase());
        List<PosLen> list = new ArrayList<>();
        aroundRun.accept(() -> {
            Optional<Found> found = switch (direction) {
                case FORWARD -> s.nextOne(spec.pattern(), pos.row(), pos.col());
                case BACKWARD -> s.previousOne(spec.pattern(), pos.row(), pos.col());
            };
            found.map(this::toPosLen).ifPresent(list::add);
        });
        return list.stream().findFirst();
    }

    @Override
    public void clear() {
        founds.clear();
    }

    @Override
    public List<PosLen> founds() {
        return founds.stream().map(this::toPosLen).toList();
    }

    @Override
    public boolean hasFounds() {
        return !founds.isEmpty();
    }

    @Override
    public void insert(long offset, int rawLen) {
        if (rawLen <= 0) return;
        shift(offset, rawLen);
    }

    @Override
    public void delete(long offset, int rawLen) {
        if (rawLen <= 0) return;
        shift(offset, -rawLen);
    }

    private void shift(long offset, int len) {

        int index = Collections.binarySearch(founds, new Found(offset, 0, 0));
        index = (index >= 0) ? index : ~index;
        if (index >= founds.size()) return;

        long end = offset + Math.abs(len);

        List<Found> sub = founds.subList(index, founds.size());
        List<Found> shifted = sub.stream()
            .filter(f -> offset >= f.offsetEnd() || f.offset() >= end)
            .map(f -> new Found(f.offset() + len, f.len(), f.rawLen()))
            .toList();
        sub.clear();

        founds.addAll(shifted);

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
