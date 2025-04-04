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

/**
 * The document search.
 * @author Naotsugu Kobayashi
 */
public interface DocumentSearch {

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

    /**
     * Run search.
     * @param spec the search specification
     * @param pos the base position
     * @param listener the found listener
     */
    void run(Spec spec, Pos pos, Progress.Listener<List<PosLen>> listener);

}
