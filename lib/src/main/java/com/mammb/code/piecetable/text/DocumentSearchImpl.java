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
import com.mammb.code.piecetable.Progress;
import com.mammb.code.piecetable.search.FoundsInChunk;
import com.mammb.code.piecetable.search.Search;
import com.mammb.code.piecetable.search.SerialSource;
import java.util.List;
import java.util.function.Consumer;

/**
 * The document search implementation.
 * @author Naotsugu Kobayashi
 */
public class DocumentSearchImpl implements DocumentSearch {

    private final SerialSource source;
    private final Consumer<Runnable> around;

    DocumentSearchImpl(SerialSource source, Consumer<Runnable> around) {
        this.source = source;
        this.around = around;
    }

    @Override
    public void run(Spec spec, Pos pos, Progress.Listener<List<PosLen>> listener) {
        around.accept(() -> {
            final Search search = search(source, spec.patternCase());
            switch (spec.direction()) {
                case FORWARD ->
                    search.search(spec.pattern(), pos.row(), pos.col(), foundsInChunk ->
                        listener.accept(Progress.of(
                            foundsInChunk.chunk().distance(),
                            foundsInChunk.chunk().parentLength(),
                            toPosLen(foundsInChunk)))
                    );
                case BACKWARD ->
                    search.searchBackward(spec.pattern(), pos.row(), pos.col(), foundsInChunk ->
                        listener.accept(Progress.of(
                            foundsInChunk.chunk().reverseDistance(),
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

    private Search search(SerialSource source, PatternCase patternCase) {
        return switch (patternCase) {
            case LITERAL -> Search.of(source);
            case CASE_INSENSITIVE -> Search.caseInsensitiveOf(source);
            case REGEX -> Search.regexOf(source);
        };
    }

}
