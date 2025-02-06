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

import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.Found;
import java.util.ArrayList;
import java.util.List;

/**
 * The naive search.
 * @author Naotsugu Kobayashi
 */
public class NaiveSearch {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public NaiveSearch(Document doc) {
        this.doc = doc;
    }

    /**
     * Search pattern.
     * Bypass String instantiation by comparing them as a byte array.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromRawCol the byte position on the row
     * @param maxFound limit of found
     * @return the list of found
     */
    List<Found> search(byte[] pattern, int fromRow, int fromRawCol, int maxFound) {

        List<Found> founds = new ArrayList<>();
        byte first = pattern[0];
        int rows = doc.rows();

        for (int row = fromRow; row < rows; row++) {
            byte[] value = doc.get(row);
            int max = value.length - pattern.length;
            int colOffset = (row == fromRow) ? fromRawCol : 0;
            for (int col = colOffset; col <= max; col++) {
                // look for first character
                if (value[col] != first) {
                    while (++col <= max && value[col] != first) ;
                }
                // found first character, now look at the rest of value
                if (col <= max) {
                    int j = col + 1;
                    int end = j + (pattern.length - 1);
                    for (int k = 1; j < end && value[j] == pattern[k]; j++, k++) ;
                    if (j == end) {
                        // found whole charSequence
                        founds.add(new Found(row, col, pattern.length));
                        if (founds.size() >= maxFound) {
                            return founds;
                        }
                        col += pattern.length;
                    }
                }
            }
        }
        return founds;
    }

    /**
     * Search pattern descending.
     * Bypass String instantiation by comparing them as a byte array.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromRawCol the byte position on the row
     * @param maxFound limit of found
     * @return the list of found
     */
    List<Found> searchDesc(byte[] pattern, int fromRow, int fromRawCol, int maxFound) {

        List<Found> founds = new ArrayList<>();
        byte last = pattern[pattern.length - 1];

        for (int row = fromRow; row >= 0; row--) {
            byte[] value = doc.get(row);
            int min = pattern.length - 1;
            int colOffset = (row == fromRow)
                ? fromRawCol - 1
                : value.length - pattern.length;
            for (int col = colOffset; col >= 0; col--) {
                // look for last character
                if (value[col] != last) {
                    while (--col >= min && value[col] != last) ;
                }
                // found last character, now look at the rest of value
                if (col >= min) {
                    int j = col - 1;
                    int end = j - (pattern.length - 1);
                    for (int k = pattern.length - 2; j > end && value[j] == pattern[k]; j--, k--) ;
                    if (j == end) {
                        // found whole charSequence
                        founds.add(new Found(row, col - (pattern.length - 1), pattern.length));
                        if (founds.size() >= maxFound) {
                            return founds;
                        }
                        col -= pattern.length;
                    }

                }
            }
        }
        return founds;
    }

}
