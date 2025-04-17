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
package com.mammb.code.piecetable.edit;

import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.PosLen;
import com.mammb.code.piecetable.SearchContext;
import com.mammb.code.piecetable.Segment;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * FlushSearchContext.
 * @author Naotsugu Kobayashi
 */
public class FlushSearchContext implements SearchContext {

    private final SearchContext sc;
    private final Flushable flushable;

    FlushSearchContext(SearchContext searchContext, Flushable flushable) {
        this.sc = searchContext;
        this.flushable = flushable;
    }

    @Override
    public void findAll(Spec spec, Consumer<Segment.Valued<List<PosLen>>> consumer) {
        sc.findAll(spec, consumer);
    }

    @Override
    public Optional<PosLen> next(Pos pos, Direction direction) {
        return sc.next(pos, direction);
    }

    @Override
    public void clear() {
        sc.clear();
    }

    @Override
    public List<PosLen> founds() {
        if (!sc.hasFounds()) return List.of();
        flushable.flush();
        return sc.founds();
    }

    @Override
    public boolean hasFounds() {
        return sc.hasFounds();
    }

    @Override
    public Optional<PosLen> findOne(Spec spec, Pos pos, Direction direction) {
        return sc.findOne(spec, pos, direction);
    }

}
