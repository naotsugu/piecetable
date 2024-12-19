/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.piecetable.edit;

import com.mammb.code.piecetable.Pos;
import com.mammb.code.piecetable.TextEdit;
import com.mammb.code.piecetable.TextEdit.Replace;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test of {@link com.mammb.code.piecetable.TextEdit#replace(List)}.
 * @author Naotsugu Kobayashi
 */
public class TextEditReplaceTest {

    @Test
    void testEmptyReplace() {
        var te = TextEdit.of();
        te.insert(0, 0, "abc");

        var pos = te.replace(List.of(Replace.of(Pos.of(0, 0), Pos.of(0, 0), "")));
        assertEquals("abc", te.getText(0, 1));
        assertEquals(Pos.of(0, 0), pos.getFirst().from());

        pos = te.replace(List.of(Replace.of(Pos.of(0, 3), Pos.of(0, 3), "")));
        assertEquals("abc", te.getText(0, 1));
        assertEquals(Pos.of(0, 3), pos.getFirst().from());
    }

    @Test
    void testSingleCharReplace() {
        var te = TextEdit.of();
        te.insert(0, 0, "abc");

        var pos = te.replace(List.of(Replace.of(Pos.of(0, 0), Pos.of(0, 1), "1")));
        assertEquals("1bc", te.getText(0, 1));
        assertEquals(Pos.of(0, 0), pos.getFirst().from());
        assertEquals(Pos.of(0, 1), pos.getFirst().to());

        pos = te.replace(List.of(Replace.of(Pos.of(0, 1), Pos.of(0, 2), "2")));
        assertEquals("12c", te.getText(0, 1));
        assertEquals(Pos.of(0, 1), pos.getFirst().from());
        assertEquals(Pos.of(0, 2), pos.getFirst().to());

        pos = te.replace(List.of(Replace.of(Pos.of(0, 2), Pos.of(0, 3), "3")));
        assertEquals("123", te.getText(0, 1));
        assertEquals(Pos.of(0, 2), pos.getFirst().from());
        assertEquals(Pos.of(0, 3), pos.getFirst().to());
    }

    @Test
    void testSingleCharDeleteReplace() {
        var te = TextEdit.of();
        te.insert(0, 0, "abc");

        var pos = te.replace(List.of(Replace.of(Pos.of(0, 1), Pos.of(0, 0), "1")));
        assertEquals("1bc", te.getText(0, 1));
        assertEquals(Pos.of(0, 1), pos.getFirst().from());
        assertEquals(Pos.of(0, 0), pos.getFirst().to());

        pos = te.replace(List.of(Replace.of(Pos.of(0, 2), Pos.of(0, 1), "2")));
        assertEquals("12c", te.getText(0, 1));
        assertEquals(Pos.of(0, 2), pos.getFirst().from());
        assertEquals(Pos.of(0, 1), pos.getFirst().to());

        pos = te.replace(List.of(Replace.of(Pos.of(0, 3), Pos.of(0, 2), "3")));
        assertEquals("123", te.getText(0, 1));
        assertEquals(Pos.of(0, 3), pos.getFirst().from());
        assertEquals(Pos.of(0, 2), pos.getFirst().to());

    }

    @Test
    void testInsertOnlyReplace() {
        var te = TextEdit.of();
        te.insert(0, 0, "abc");

        var pos = te.replace(List.of(Replace.of(Pos.of(0, 0), Pos.of(0, 0), "1")));
        assertEquals("1abc", te.getText(0, 1));
        assertEquals(Pos.of(0, 1), pos.getFirst().from());
        assertTrue(pos.getFirst().isMono());

        pos = te.replace(List.of(Replace.of(Pos.of(0, 1), Pos.of(0, 1), "23")));
        assertEquals("123abc", te.getText(0, 1));
        assertEquals(Pos.of(0, 3), pos.getFirst().from());
        assertTrue(pos.getFirst().isMono());
    }

    @Test
    void testMultiCharSameWidthReplace() {
        var te = TextEdit.of();
        te.insert(0, 0, "abc");

        var pos = te.replace(List.of(
            Replace.of(Pos.of(0, 0), Pos.of(0, 1), "1"),
            Replace.of(Pos.of(0, 2), Pos.of(0, 3), "3")));

        assertEquals("1b3", te.getText(0, 1));
        assertEquals(Pos.of(0, 0), pos.getFirst().from());
        assertEquals(Pos.of(0, 1), pos.getFirst().to());
        //assertEquals(Pos.of(0, 2), pos.get(1).from());
        assertEquals(Pos.of(0, 3), pos.get(1).to());

    }

//    @Test
//    void testMultiCharReplace() {
//        var te = TextEdit.of();
//        te.insert(0, 0, "abc");
//
//        var pos = te.replace(List.of(
//            Replace.of(Pos.of(0, 0), Pos.of(0, 1), "12"),
//            Replace.of(Pos.of(0, 2), Pos.of(0, 3), "34")));
//        assertEquals("12b34", te.getText(0, 1));
//        assertEquals(Pos.of(0, 2), pos.getFirst());
//        assertEquals(Pos.of(0, 5), pos.get(1));
//
//    }

}
