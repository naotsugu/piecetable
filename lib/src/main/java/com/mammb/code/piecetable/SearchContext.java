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
package com.mammb.code.piecetable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The search context.
 * @author Naotsugu Kobayashi
 */
public interface SearchContext {

    /** pattern case. */
    enum PatternCase {
        /** literal. */
        LITERAL,
        /** case-insensitive. */
        CASE_INSENSITIVE,
        /** regex. */
        REGEX
    }

    /** direction. */
    enum Direction {
        /** forward. */
        FORWARD,
        /** backward. */
        BACKWARD
    }

    /**
     * Search specification.
     * @param pattern the pattern sequence
     * @param patternCase the pattern case
     * @param direction the direction
     */
    record Spec(CharSequence pattern, PatternCase patternCase, Direction direction) { }

    void findAll(Spec spec, Consumer<Segment.Valued<List<PosLen>>> consumer);

    Optional<PosLen> findNext(Pos pos);

    Optional<PosLen> findPrevious(Pos pos);

    Optional<PosLen> findNext(Spec spec, Pos pos);

    void clear();

    List<PosLen> currentFounds();

}
