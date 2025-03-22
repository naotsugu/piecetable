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

import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.Findable.Found;
import com.mammb.code.piecetable.Findable.FoundListener;

/**
 * The naive search.
 * Bypass String instantiation by comparing them as a byte array.
 * @author Naotsugu Kobayashi
 */
public class NaiveSearch implements Search {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public NaiveSearch(Document doc) {
        this.doc = doc;
    }

    @Override
    public void search(CharSequence cs, int fromRow, int fromCol, FoundListener listener) {

        if (cs == null || cs.isEmpty()) return;

        byte[] pattern = cs.toString().getBytes(doc.charset());
        int fromRawCol =  doc.getText(fromRow).toString().substring(0, fromCol).getBytes().length;

        byte first = pattern[0];
        int rows = doc.rows();

        for (int row = fromRow; row < rows; row++) {
            byte[] value = doc.get(row);
            int max = value.length - pattern.length;
            int from = (row == fromRow) ? fromRawCol : 0;
            for (int col = from; col <= max; col++) {
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
                        var found = new Found(row, new String(value, 0, col, doc.charset()).length(), cs.length());
                        if (!listener.apply(found)) {
                            return;
                        }
                        col += pattern.length;
                    }
                }
            }
        }
    }

    @Override
    public void searchDesc(CharSequence cs, int fromRow, int fromCol, FoundListener listener) {

        if (cs == null || cs.isEmpty()) return;

        byte[] pattern = cs.toString().getBytes(doc.charset());
        int fromRawCol =  doc.getText(fromRow).toString().substring(0, fromCol).getBytes().length;

        byte last = pattern[pattern.length - 1];

        for (int row = fromRow; row >= 0; row--) {
            byte[] value = doc.get(row);
            int min = pattern.length - 1;
            int from = (row == fromRow)
                ? fromRawCol - 1
                : value.length - pattern.length;
            for (int col = from; col >= 0; col--) {
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
                        int c = col - (pattern.length - 1);
                        var found = new Found(row, new String(value, 0, c, doc.charset()).length(), cs.length());
                        if (!listener.apply(found)) {
                            return;
                        }
                        col -= pattern.length;
                    }

                }
            }
        }
    }

}
