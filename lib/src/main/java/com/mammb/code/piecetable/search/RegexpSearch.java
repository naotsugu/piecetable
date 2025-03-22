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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The regexp search.
 * @author Naotsugu Kobayashi
 */
public class RegexpSearch implements Search {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public RegexpSearch(Document doc) {
        this.doc = doc;
    }

    @Override
    public void search(CharSequence pattern, int fromRow, int fromCol, FoundListener listener) {

        if (pattern == null || pattern.isEmpty()) return;

        Pattern p = Pattern.compile(pattern.toString());

        for (int row = fromRow; row < doc.rows(); row++) {

            Matcher m = p.matcher(doc.getText(row));

            while (m.find()) {
                if (row == fromRow && m.start() < fromCol) {
                    continue;
                }
                var found = new Found(row, m.start(), m.end() - m.start());
                if (!listener.apply(found)) {
                    return;
                }
            }
        }
    }

    @Override
    public void searchDesc(CharSequence pattern, int fromRow, int fromCol, FoundListener listener) {

        if (pattern == null || pattern.isEmpty()) return;

        Pattern p = Pattern.compile(pattern.toString());

        for (int row = fromRow; row >= 0; row--) {

            List<Found> rowFounds = new ArrayList<>();
            Matcher m = p.matcher(doc.getText(row));

            while (m.find()) {
                if (row == fromRow && m.start() >= fromCol) {
                    break;
                }
                rowFounds.addFirst(new Found(row, m.start(), m.end() - m.start()));
            }

            for (Found found : rowFounds) {
                if (!listener.apply(found)) {
                    return;
                }
            }
        }
    }

}
