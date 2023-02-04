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
package com.mammb.code.piecetable;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link PieceTable}.
 * @author Naotsugu Kobayashi
 */
class PieceTableTest {

    @Test
    void testEdit() {

        var table = PieceTable.of("abc");
        // |a|b|c|
        // |0|1|2|
        assertEquals("abc", table.substring(0, 3));

        table.insert(3, "def");
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|
        assertEquals("abc" + "def", table.toString());

        table.insert(6, "ghi");
        // |a|b|c| |d|e|f| |g|h|i|
        // |0|1|2| |3|4|5| |6|7|8|
        assertEquals("abc" + "def" + "ghi", table.toString());

        table.delete(2, 2);
        // |a|b| |e|f| |g|h|i|
        // |0|1| |2|3| |4|5|6|
        assertEquals("ab" + "ef" + "ghi", table.toString());

        table.insert(0, "**");
        // |*|*| |a|b| |e|f| |g|h|i|
        // |0|1| |2|3| |4|5| |6|7|8|
        assertEquals("**" + "ab" + "ef" + "ghi", table.toString());

        table.insert(9, "**");
        // |*|*| |a|b| |e|f| |g|h|i| |*|*|
        // |0|1| |2|3| |4|5| |6|7|8| |9|A|
        assertEquals("**" + "ab" + "ef" + "ghi" + "**", table.toString());
        assertEquals("b", table.substring(3, 4));
        assertEquals("be", table.substring(3, 5));
        assertEquals("bef", table.substring(3, 6));
        assertEquals("befg", table.substring(3, 7));
        assertEquals("befgh", table.substring(3, 8));
        assertEquals("befghi", table.substring(3, 9));
        assertEquals("befghi*", table.substring(3, 10));
        assertEquals("befghi**", table.substring(3, 11));

        table.delete(4, 5);
        // |*|*| |a|b| |*|*|
        // |0|1| |2|3| |4|5|
        assertEquals("**" + "ab" + "**", table.toString());

        table.delete(0, 6);
        // |
        // |
        assertEquals("", table.toString());

        table.insert(0, "①②③456");
        // |①|②|③|4|5|6|
        // |０|１|２|3|4|5|
        assertEquals("①②③456", table.toString());

        table.delete(2, 2);
        // |①|②|5|6|
        // |０|１|4|5|
        assertEquals("①②56", table.toString());
    }

    @Test
    void testUndoRedo() {

        var table = PieceTable.of("abc");
        // |a|b|c|
        // |0|1|2|
        assertEquals("abc", table.toString());

        table.insert(3, "def");
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|
        assertEquals("abc" + "def", table.toString());

        table.delete(2, 2);
        // |a|b| |e|f|
        // |0|1| |2|3|
        assertEquals("ab" + "ef", table.toString());

        table.undo();
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|
        assertEquals("abc" + "def", table.toString());

        table.undo();
        // |a|b|c|
        // |0|1|2|
        assertEquals("abc", table.toString());

        table.redo();
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|
        assertEquals("abc" + "def", table.toString());

        table.insert(2, "***");
        // |a|b| |*|*|*| |c| |d|e|f|
        // |0|1| |2|3|4| |5| |6|7|8|
        assertEquals("ab" + "***" + "c" + "def", table.toString());

        table.redo();
        assertEquals("ab" + "***" + "c" + "def", table.toString());

        table.undo();
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|
        assertEquals("abc" + "def", table.toString());

    }

    @Test
    void testBytesUntil() {
        var table = PieceTable.of("abc");
        table.insert(3, "def");
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|

        assertArrayEquals("a".getBytes(StandardCharsets.UTF_8),
            table.bytes(0, b-> b[0] == 'a'));
        assertArrayEquals("ab".getBytes(StandardCharsets.UTF_8),
            table.bytes(0, b-> b[0] == 'b'));
        assertArrayEquals("abc".getBytes(StandardCharsets.UTF_8),
            table.bytes(0, b-> b[0] == 'c'));

        assertArrayEquals("bcde".getBytes(StandardCharsets.UTF_8),
            table.bytes(1, b-> b[0] == 'e'));
        assertArrayEquals("cde".getBytes(StandardCharsets.UTF_8),
            table.bytes(2, b-> b[0] == 'e'));
        assertArrayEquals("de".getBytes(StandardCharsets.UTF_8),
            table.bytes(3, b-> b[0] == 'e'));

        assertArrayEquals("ef".getBytes(StandardCharsets.UTF_8),
            table.bytes(4, b-> b[0] == 'x'));
    }

    @Test
    void testBytesUntilBefore() {
        var table = PieceTable.of("abc");
        table.insert(3, "def");
        // |a|b|c| |d|e|f|
        // |0|1|2| |3|4|5|

        assertArrayEquals("abcdef".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(6, b-> b[0] == 'x'));
        assertArrayEquals("bcdef".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(6, b-> b[0] == 'a'));
        assertArrayEquals("bcde".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(5, b-> b[0] == 'a'));
        assertArrayEquals("bcd".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(4, b-> b[0] == 'a'));
        assertArrayEquals("bc".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(3, b-> b[0] == 'a'));
        assertArrayEquals("b".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(2, b-> b[0] == 'a'));
        assertArrayEquals("".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(1, b-> b[0] == 'a'));

        assertArrayEquals("cdef".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(6, b-> b[0] == 'b'));
        assertArrayEquals("cde".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(5, b-> b[0] == 'b'));
        assertArrayEquals("cd".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(4, b-> b[0] == 'b'));
        assertArrayEquals("c".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(3, b-> b[0] == 'b'));
        assertArrayEquals("".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(2, b-> b[0] == 'b'));
        assertArrayEquals("a".getBytes(StandardCharsets.UTF_8),
            table.bytesBefore(1, b-> b[0] == 'b'));
    }

}
