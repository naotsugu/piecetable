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
import com.mammb.code.piecetable.Findable.Found;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link CaseSensitiveSearch}.
 * @author Naotsugu Kobayashi
 */
class CaseSensitiveSearchTest {

    @Test
    void search() {

        var doc = Document.of();
        doc.insert(0, 0, "aBc12345678\n");
        doc.insert(1, 0, "12345aBc678\n");
        doc.insert(2, 0, "aBc45678aBc\n");

        List<Found> founds = new ArrayList<>();
        new CaseSensitiveSearch(doc).search("AbC", 0, 0, founds::add);
        assertEquals(4, founds.size());

        assertEquals(0, founds.get(0).row());
        assertEquals(0, founds.get(0).col());

        assertEquals(1, founds.get(1).row());
        assertEquals(5, founds.get(1).col());

        assertEquals(2, founds.get(2).row());
        assertEquals(0, founds.get(2).col());

        assertEquals(2, founds.get(3).row());
        assertEquals(8, founds.get(3).col());

    }

    @Test
    void searchDesc() {

        var doc = Document.of();
        doc.insert(0, 0, "aBc12345678\n");
        doc.insert(1, 0, "12345aBc678\n");
        doc.insert(2, 0, "aBc45678aBc\n");

        List<Found> founds = new ArrayList<>();
        new CaseSensitiveSearch(doc).searchDesc("AbC", 2, 12, founds::add);
        assertEquals(4, founds.size());

        assertEquals(2, founds.get(0).row());
        assertEquals(8, founds.get(0).col());

        assertEquals(2, founds.get(1).row());
        assertEquals(0, founds.get(1).col());

        assertEquals(1, founds.get(2).row());
        assertEquals(5, founds.get(2).col());

        assertEquals(0, founds.get(3).row());
        assertEquals(0, founds.get(3).col());

    }
}
