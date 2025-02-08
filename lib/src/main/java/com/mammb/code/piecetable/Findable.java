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

/**
 * The findable.
 * @author Naotsugu Kobayashi
 */
public interface Findable {

    /**
     * Searches for the specified char sequence.
     * @param cs the specified char sequence
     * @param caseSensitive case-sensitive?
     * @return found list
     */
    List<Found> findAll(CharSequence cs, boolean caseSensitive);

    /**
     * Searches for the specified regular expression.
     * @param regex the regular expression to which this string is to be matched
     * @return found list
     */
    List<Found> findAll(CharSequence regex);

    /**
     * Searches for the specified char sequence.
     * @param cs the specified char sequence
     * @param row the number of start row(zero origin)
     * @param col the start byte position on the row
     * @param forward forward?
     * @param caseSensitive case-sensitive?
     * @return the found
     */
    Optional<Found> find(CharSequence cs, int row, int col, boolean forward, boolean caseSensitive);

    /**
     * Searches for the specified regular expression.
     * @param regex the regular expression to which this string is to be matched
     * @param row the number of start row(zero origin)
     * @param col the start byte position on the row
     * @param forward forward?
     * @return the found
     */
    Optional<Found> find(CharSequence regex, int row, int col, boolean forward);

    /**
     * Searches for the specified char sequence.
     * @param cs the specified char sequence
     * @param caseSensitive case-sensitive?
     * @param row the number of start row(zero origin)
     * @param col the start byte position on the row
     * @param forward forward?
     * @param listener the found listener
     */
    void find(CharSequence cs, boolean caseSensitive, int row, int col, boolean forward, FoundListener listener);

    /**
     * Searches for the specified regular expression.
     * @param regex the regular expression to which this string is to be matched
     * @param row the number of start row(zero origin)
     * @param col the start byte position on the row
     * @param forward forward?
     * @param listener the found listener
     */
    void find(CharSequence regex, int row, int col, boolean forward, FoundListener listener);

    /**
     * The found.
     * @param row the number of row
     * @param col the byte position on the row
     * @param len the byte length
     * @author Naotsugu Kobayashi
     */
    record Found(int row, int col, int len) { }

    /**
     * Found listener.
     */
    interface FoundListener {
        /**
         * Apply the found event.
         * @param found the found
         * @return {@code true}, if the current search can be continued
         */
        boolean apply(Found found);
    }

}
