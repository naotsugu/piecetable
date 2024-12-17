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
package com.mammb.code.piecetable.text;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link RowIndex}.
 * @author Naotsugu Kobayashi
 */
class RowIndexTest {

    @Test
    void add() {

        var index = RowIndex.of();

        index.add("a".getBytes());
        // a
        assertEquals(1, index.rowLengths().length);
        assertEquals(1, index.rowLengths()[0]);      // |a|

        index.add("b".getBytes());
        // ab
        assertEquals(1, index.rowLengths().length);
        assertEquals(2, index.rowLengths()[0]);      // |a|b|

        index.add("\n".getBytes());
        // ab$
        assertEquals(2, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);      // |a|b|$|
        assertEquals(0, index.rowLengths()[1]);      // |

        index.add("cd".getBytes());
        // ab$cd
        assertEquals(2, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);      // |a|b|$|
        assertEquals(2, index.rowLengths()[1]);      // |c|d|

        index.add("ef\n\ngh".getBytes());
        // ab$cdef$$ij
        assertEquals(4, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);      // |a|b|$|
        assertEquals(5, index.rowLengths()[1]);      // |c|d|e|f|$|
        assertEquals(1, index.rowLengths()[2]);      // |$|
        assertEquals(2, index.rowLengths()[3]);      // |g|h|

    }

    @Test
    void get() {
        var index = RowIndex.of(5);
        index.add("ab\n\ncde\nf\ng\nhi\njkl\nmn".getBytes());
        assertEquals( 0, index.get(0));  // |a|b|$|    3    3
        assertEquals( 3, index.get(1));  // |$|        1    4
        assertEquals( 4, index.get(2));  // |c|d|e|$|  4    8
        assertEquals( 8, index.get(3));  // |f|$|      2   10
        assertEquals(10, index.get(4));  // |g|$|      2   12
        assertEquals(12, index.get(5));  // |h|i|$|    3   15
        assertEquals(15, index.get(6));  // |j|k|l|$|  4   19
        assertEquals(19, index.get(7));  // |m|n|      2   21

        assertEquals(2, index.stCache().length);
        assertEquals(0, index.stCache()[0]);
        assertEquals(12, index.stCache()[1]);
    }

    @Test
    void insert() {
        var index = RowIndex.of(5);
        index.add("ab\ncde\nfg\nhij\nk".getBytes());
        assertEquals(5, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);
        assertEquals(4, index.rowLengths()[1]);
        assertEquals(3, index.rowLengths()[2]);
        assertEquals(4, index.rowLengths()[3]);
        assertEquals(1, index.rowLengths()[4]);
        // |a|b|$|                    |a|b|$|
        // |c|d|e|$|   |1|2|$|        |c|d|1|2|$|
        //     ^------ |3|4|$|        |3|4|$|
        //             |5|6|          |5|6|e|$|
        // |f|g|$|                    |f|g|$|
        // |h|i|j|$|                  |h|i|j|$|
        // |k|                        |k|
        index.insert(1, 2, "12\n24\n56".getBytes());

        assertEquals(7, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);
        assertEquals(5, index.rowLengths()[1]);
        assertEquals(3, index.rowLengths()[2]);
        assertEquals(4, index.rowLengths()[3]);
        assertEquals(3, index.rowLengths()[4]);
        assertEquals(4, index.rowLengths()[5]);
        assertEquals(1, index.rowLengths()[6]);
    }

    @Test
    void insertMono() {
        var index = RowIndex.of(5);
        index.insert(0, 0, "abc".getBytes());
        assertEquals(1, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);
    }

    @Test
    void insertDelete() {
        var index = RowIndex.of(5);
        index.insert(0, 0, "abc".getBytes());
        index.delete(0, 0, 3);
        assertEquals(1, index.rowLengths().length);
        assertEquals(0, index.rowLengths()[0]);
        index.insert(0, 0, "123".getBytes());
        index.delete(0, 0, 3);
        assertEquals(1, index.rowLengths().length);
        assertEquals(0, index.rowLengths()[0]);
    }

    @Test
    void deleteEnd() {
        var index = RowIndex.of(5);
        index.add("12".getBytes());
        index.delete(0, 1, 1);
        assertEquals(1, index.rowLengths().length);
        assertEquals(1, index.rowLengths()[0]);

        index.delete(0, 0, 1);
        assertEquals(1, index.rowLengths().length);
        assertEquals(0, index.rowLengths()[0]);
    }

    @Test
    void deleteEnd2() {
        var index = RowIndex.of(5);
        index.add("abc\n12".getBytes());
        index.delete(1, 1, 1);
        assertEquals(2, index.rowLengths().length);
        assertEquals(4, index.rowLengths()[0]);
        assertEquals(1, index.rowLengths()[1]);
    }

    @Test
    void deleteWithinSingleLine() {
        var index = RowIndex.of(5);
        index.add("abcd\n".getBytes());
        index.delete(0, 1, 2);
        // abcd$ -> ad$
        assertEquals(2, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);
        assertEquals(0, index.rowLengths()[1]);
        index.delete(0, 0, 3);
        // ad$ ->
        assertEquals(1, index.rowLengths().length);
        assertEquals(0, index.rowLengths()[0]);
    }

    @Test
    void deleteAcrossMultipleLines() {
        var index = RowIndex.of(5);
        index.add("ab\ncd\nef\ngh\n".getBytes());
        index.delete(0, 1, 6);
        assertEquals(3, index.rowLengths().length);
        assertEquals(3, index.rowLengths()[0]);
        assertEquals(3, index.rowLengths()[1]);
        assertEquals(0, index.rowLengths()[2]);
    }

    @Test
    void rowSizeWithInsertAndDelete() {

        var index = RowIndex.of(5);
        assertEquals(1, index.rowSize());

        index.insert(0, 0, "aaa\nbbb\nccc".getBytes(StandardCharsets.UTF_8));
        assertEquals(3, index.rowSize());

        int[] rowLengths = index.rowLengths();
        assertEquals(3, rowLengths.length);
        assertEquals(4, rowLengths[0]);
        assertEquals(4, rowLengths[1]);
        assertEquals(3, rowLengths[2]);

        assertEquals(0, index.get(0));
        assertEquals(4, index.get(1));
        assertEquals(8, index.get(2));

        index.delete(0, 0, 8);
        // aaa$bbb$ccc -> ccc
        // ^^^^^^^^
        // -------------------------
        // aaa$ : 4       ccc : 3
        // bbb$ : 4
        // ccc  : 3
        assertEquals(1, index.rowSize());

        rowLengths = index.rowLengths();
        assertEquals(1, rowLengths.length);
        assertEquals(3, rowLengths[0]);

        assertEquals(0, index.get(0));

    }

    @Test
    void rowSizeWithInsertAndDelete2() {

        var index = RowIndex.of(5);
        assertEquals(1, index.rowSize());

        index.insert(0, 0, "aaa\nbbb\nccc".getBytes(StandardCharsets.UTF_8));
        assertEquals(3, index.rowSize());

        index.delete(0, 1, 8);
        // aaa$bbb$ccc -> acc
        //  ^^^^^^^^
        // -------------------------
        // aaa$ : 4       acc : 3
        // bbb$ : 4
        // ccc  : 3
        assertEquals(1, index.rowSize());

        int[] rowLengths = index.rowLengths();
        assertEquals(1, rowLengths.length);
        assertEquals(3, rowLengths[0]);

        assertEquals(0, index.get(0));

    }

    @Test
    void rowSizeWithInsertAndDelete3() {

        var index = RowIndex.of(5);
        assertEquals(1, index.rowSize());

        index.insert(0, 0, "aaa\nbbb\nccc".getBytes(StandardCharsets.UTF_8));
        assertEquals(3, index.rowSize());

        index.delete(0, 3, 8);
        // aaa$bbb$ccc -> acc
        //    ^^^^^^^^
        // -------------------------
        // aaa$ : 4       aaa : 3
        // bbb$ : 4
        // ccc  : 3
        assertEquals(1, index.rowSize());

        int[] rowLengths = index.rowLengths();
        assertEquals(1, rowLengths.length);
        assertEquals(3, rowLengths[0]);

        assertEquals(0, index.get(0));

    }

    @Test
    void rowSizeWithInsertAndDelete4() {

        var index = RowIndex.of(5);
        assertEquals(1, index.rowSize());

        index.insert(0, 0, "aaa\nbbb\nccc".getBytes(StandardCharsets.UTF_8));
        assertEquals(3, index.rowSize());

        index.delete(1, 0, 7);
        // aaa$bbb$ccc -> aaa$
        //     ^^^^^^^
        // -------------------------
        // aaa$ : 4       aaa$ : 4
        // bbb$ : 4            : 1
        // ccc  : 3
        assertEquals(2, index.rowSize());

        int[] rowLengths = index.rowLengths();
        assertEquals(2, rowLengths.length);
        assertEquals(4, rowLengths[0]);
        assertEquals(0, rowLengths[1]);

        assertEquals(0, index.get(0));
        assertEquals(4, index.get(1));

    }

    @Test
    void lines() {

        int[] ret = RowIndex.rows("".getBytes());
        assertEquals(0, ret.length);

        ret = RowIndex.rows("a".getBytes());
        assertEquals(1, ret.length);
        assertEquals(1, ret[0]);

        ret = RowIndex.rows("ab".getBytes());
        assertEquals(1, ret.length);
        assertEquals(2, ret[0]);

        ret = RowIndex.rows("ab\n".getBytes());
        assertEquals(2, ret.length);
        assertEquals(3, ret[0]);
        assertEquals(0, ret[1]);

        ret = RowIndex.rows("\n".getBytes());
        assertEquals(2, ret.length);
        assertEquals(1, ret[0]);
        assertEquals(0, ret[1]);

        ret = RowIndex.rows("\n\n".getBytes());
        assertEquals(3, ret.length);
        assertEquals(1, ret[0]);
        assertEquals(1, ret[1]);
        assertEquals(0, ret[2]);

        ret = RowIndex.rows("abc\r\nde\r\nf".getBytes());
        assertEquals(3, ret.length);
        assertEquals(5, ret[0]);
        assertEquals(4, ret[1]);
        assertEquals(1, ret[2]);
    }

    @Test
    void serial() {
        var index = RowIndex.of(3);
        index.insert(0, 0, "a\nbb\nccc\ndddd\neeeee".getBytes(StandardCharsets.UTF_8));

        assertEquals(0L, index.serial(0, 0));
        assertEquals(1L, index.serial(0, 1));

        assertEquals(2L, index.serial(1, 0));
        assertEquals(3L, index.serial(1, 1));
        assertEquals(4L, index.serial(1, 2));

        assertEquals(5L, index.serial(2, 0));
        assertEquals(6L, index.serial(2, 1));
        assertEquals(7L, index.serial(2, 2));
        assertEquals(8L, index.serial(2, 3));

        assertEquals(9L,  index.serial(3, 0));
        assertEquals(10L, index.serial(3, 1));
        assertEquals(11L, index.serial(3, 2));
        assertEquals(12L, index.serial(3, 3));
        assertEquals(13L, index.serial(3, 4));

        assertEquals(14L, index.serial(4, 0));
        assertEquals(15L, index.serial(4, 1));
        assertEquals(16L, index.serial(4, 2));
        assertEquals(17L, index.serial(4, 3));
        assertEquals(18L, index.serial(4, 4));
        assertEquals(19L, index.serial(4, 5));
    }

    @Test
    void pos() {
        var index = RowIndex.of(3);
        index.insert(0, 0, "a\nbb\nccc\ndddd\neeeee".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals(new int[] {0, 0}, index.pos(0));
        assertArrayEquals(new int[] {0, 1}, index.pos(1));

        assertArrayEquals(new int[] {1, 0}, index.pos(2));
        assertArrayEquals(new int[] {1, 1}, index.pos(3));
        assertArrayEquals(new int[] {1, 2}, index.pos(4));

        assertArrayEquals(new int[] {2, 0}, index.pos(5));
        assertArrayEquals(new int[] {2, 1}, index.pos(6));
        assertArrayEquals(new int[] {2, 2}, index.pos(7));
        assertArrayEquals(new int[] {2, 3}, index.pos(8));

        assertArrayEquals(new int[] {3, 0}, index.pos(9));
        assertArrayEquals(new int[] {3, 1}, index.pos(10));
        assertArrayEquals(new int[] {3, 2}, index.pos(11));
        assertArrayEquals(new int[] {3, 3}, index.pos(12));
        assertArrayEquals(new int[] {3, 4}, index.pos(13));

        assertArrayEquals(new int[] {4, 0}, index.pos(14));
        assertArrayEquals(new int[] {4, 1}, index.pos(15));
        assertArrayEquals(new int[] {4, 2}, index.pos(16));
        assertArrayEquals(new int[] {4, 3}, index.pos(17));
        assertArrayEquals(new int[] {4, 4}, index.pos(18));
        assertArrayEquals(new int[] {4, 5}, index.pos(19));
    }

}
