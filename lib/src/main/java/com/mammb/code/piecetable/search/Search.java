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
import com.mammb.code.piecetable.Findable.FoundListener;

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
    void search(CharSequence pattern, int fromRow, int fromCol, FoundListener listener);

    /**
     * Search pattern descending.
     * @param pattern the pattern
     * @param fromRow from row
     * @param fromCol from column
     * @param listener the found listener
     */
    void searchDesc(CharSequence pattern, int fromRow, int fromCol, FoundListener listener);

    /**
     * Create a case-sensitive search.
     * @param doc the document
     * @return the search
     */
    static Search of(Document doc) {
        return new NaiveSearch(doc);
    }

    /**
     * Create a case-insensitive search.
     * @param doc the document
     * @return the search
     */
    static Search caseInsensitiveOf(Document doc) {
        return new CaseInsensitiveSearch(doc);
    }

    /**
     * Create a regexp search.
     * @param doc the document
     * @return the search
     */
    static Search regexpOf(Document doc) {
        return new RegexpSearch(doc);
    }

}
