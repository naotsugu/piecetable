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
 * The case-sensitive search.
 * @author Naotsugu Kobayashi
 */
public class CaseSensitiveSearch {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public CaseSensitiveSearch(Document doc) {
        this.doc = doc;
    }

    /**
     * Search pattern.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromCol from column
     * @param maxFound limit of found
     * @return the list of found
     */
    List<Found> search(String pattern, int fromRow, int fromCol, int maxFound) {

        List<Found> founds = new ArrayList<>();
        pattern = pattern.toLowerCase();
        char first = pattern.charAt(0);

        for (int row = fromRow; row < doc.rows(); row++) {

            CharSequence cs = doc.getText(row);
            int max = cs.length() - pattern.length();
            int from = (row == fromRow) ? fromCol : 0;
            for (int col = from; col <= max; col++) {
                // look for first character
                if (Character.toLowerCase(cs.charAt(col)) != first) {
                    while (++col <= max && Character.toLowerCase(cs.charAt(col)) != first) ;
                }
                // found first character, now look at the rest of value
                if (col <= max) {
                    int j = col + 1;
                    int end = j + (pattern.length() - 1);
                    for (int k = 1; j < end && Character.toLowerCase(cs.charAt(j)) == pattern.charAt(k); j++, k++) ;
                    if (j == end) {
                        // found whole charSequence
                        founds.add(new Found(row, col, pattern.length()));
                        if (founds.size() >= maxFound) {
                            return founds;
                        }
                        col += pattern.length();
                    }
                }

            }
        }

        return founds;
    }

    /**
     * Search pattern descending.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromCol from column
     * @param maxFound limit of found
     * @return the list of found
     */
    List<Found> searchDesc(String pattern, int fromRow, int fromCol, int maxFound) {

        List<Found> founds = new ArrayList<>();
        pattern = pattern.toLowerCase();
        char last = pattern.charAt(pattern.length() - 1);

        for (int row = fromRow; row >= 0; row--) {
            CharSequence cs = doc.getText(row);
            int min = pattern.length() - 1;
            int from = (row == fromRow)
                ? fromCol - 1
                : cs.length() - pattern.length();
            for (int col = from; col >= 0; col--) {
                // look for last character
                if (Character.toLowerCase(cs.charAt(col)) != last) {
                    while (--col >= min && Character.toLowerCase(cs.charAt(col)) != last) ;
                }
                // found last character, now look at the rest of value
                if (col >= min) {
                    int j = col - 1;
                    int end = j - (pattern.length() - 1);
                    for (int k = pattern.length() - 2; j > end && Character.toLowerCase(cs.charAt(j)) == pattern.charAt(k); j--, k--) ;
                    if (j == end) {
                        // found whole charSequence
                        founds.add(new Found(row, col - (pattern.length() - 1), pattern.length()));
                        if (founds.size() >= maxFound) {
                            return founds;
                        }
                        col -= pattern.length();
                    }

                }
            }
        }
        return founds;
    }

}
