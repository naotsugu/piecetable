/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * StringMetrics.
 *
 * <pre>
 *                 0   1            0   1   2             0   1   2   3
 *   string      | a | b |        | a | b | $ |         | a | b | $ | c |
 *
 *   Display     1 : ab           1 : ab$               1 : ab$
 *   on screen                    2 :                   2 : c
 *
 *   rowAnchor   [0]              [0, 3]                [0, 3]
 * </pre>
 *
 * @author Naotsugu Kobayashi
 */
public class StringMetrics {

    /** The metrics source. */
    private final CharSequence source;

    /** The code point count. */
    private int codePointCount = -1;

    /** The row start index list. */
    private final List<Integer> rowAnchor = new ArrayList<>();


    /**
     * Constructor.
     * @param source the metrics source
     */
    public StringMetrics(CharSequence source) {
        this.source = source;
    }

    /**
     * Get statistics disabled or not.
     * @return {@code true} if disabled
     */
    boolean isDisabled() {
        return codePointCount < 0 || rowAnchor.isEmpty();
    }

    /**
     * Clear this figure.
     */
    void clear() {
        codePointCount = -1;
        rowAnchor.clear();
    }

    /**
     * Creates an anchor at the line start position
     * and code point count from the specified text content.
     */
    void init() {
        rowAnchor.clear();
        rowAnchor.add(0);
        int cpCount = 0;
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (Character.isHighSurrogate(ch)) {
                cpCount++;
            }
            if (ch == '\n') {
                rowAnchor.add(i + 1);
            }
        }
        codePointCount = cpCount;

    }

    /**
     * Get the row size of this view text.
     * @return the row size
     */
    int rowSize() {
        if (isDisabled()) throw new IllegalStateException("not initialized");
        return rowAnchor.size();
    }

    /**
     * Get the code point count of this view text.
     * @return the code point count
     */
    int codePointCount() {
        if (isDisabled()) throw new IllegalStateException("not initialized");
        return codePointCount;
    }

    /**
     * Get the row index of this view text.
     * @param row the number of row. zero origin
     * @return the row index
     */
    int rowIndex(int row) {
        if (isDisabled()) throw new IllegalStateException("not initialized");
        if (row >= rowAnchor.size())
            throw new IllegalArgumentException(
                "out of index.[row:%d][rowAnchor.size:%d]".formatted(row, rowAnchor.size()));
        return rowAnchor.get(row);
    }

}
