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

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The search.
 * @author Naotsugu Kobayashi
 */
public interface Search {

    /**
     * Search pattern.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromCol from column
     * @param listener the found listener
     */
    void search(CharSequence pattern, int fromRow, int fromCol,
            Function<FoundsInChunk, Boolean> listener);

    /**
     * Search pattern backward.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromCol from column
     * @param listener the found listener
     */
    void searchBackward(CharSequence pattern, int fromRow, int fromCol,
            Function<FoundsInChunk, Boolean> listener);

    /**
     * Create a case-sensitive search.
     * @param source the search source
     * @return the search
     */
    static Search of(SerialSource source) {
        return new PatternSearch(source, Pattern.LITERAL);
    }

    /**
     * Create a case-insensitive search.
     * @param source the search source
     * @return the search
     */
    static Search caseInsensitiveOf(SerialSource source) {
        return new PatternSearch(source, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
    }

    /**
     * Create a regexp search.
     * @param source the search source
     * @return the search
     */
    static Search regexOf(SerialSource source) {
        return new PatternSearch(source, 0);
    }
}
