package com.mammb.dev.picetable.index;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineIndexTest {

    @Test
    void add() {

        var index = LineIndex.of();

        index.add("a".getBytes());
        // a
        assertEquals(1, index.lineLengths().length);
        assertEquals(1, index.lineLengths()[0]);      // |a|

        index.add("b".getBytes());
        // ab
        assertEquals(1, index.lineLengths().length);
        assertEquals(2, index.lineLengths()[0]);      // |a|b|

        index.add("\n".getBytes());
        // ab$
        assertEquals(2, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);      // |a|b|$|
        assertEquals(0, index.lineLengths()[1]);      // |

        index.add("cd".getBytes());
        // ab$cd
        assertEquals(2, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);      // |a|b|$|
        assertEquals(2, index.lineLengths()[1]);      // |c|d|

        index.add("ef\n\ngh".getBytes());
        // ab$cdef$$ij
        assertEquals(4, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);      // |a|b|$|
        assertEquals(5, index.lineLengths()[1]);      // |c|d|e|f|$|
        assertEquals(1, index.lineLengths()[2]);      // |$|
        assertEquals(2, index.lineLengths()[3]);      // |g|h|

    }

    @Test
    void get() {
        var index = LineIndex.of(5);
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
        var index = LineIndex.of(5);
        index.add("ab\ncde\nfg\nhij\nk".getBytes());
        assertEquals(5, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);
        assertEquals(4, index.lineLengths()[1]);
        assertEquals(3, index.lineLengths()[2]);
        assertEquals(4, index.lineLengths()[3]);
        assertEquals(1, index.lineLengths()[4]);
        // |a|b|$|                    |a|b|$|
        // |c|d|e|$|   |1|2|$|        |c|d|1|2|$|
        //     ^------ |3|4|$|        |3|4|$|
        //             |5|6|          |5|6|e|$|
        // |f|g|$|                    |f|g|$|
        // |h|i|j|$|                  |h|i|j|$|
        // |k|                        |k|
        index.insert(1, 2, "12\n24\n56".getBytes());

        assertEquals(7, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);
        assertEquals(5, index.lineLengths()[1]);
        assertEquals(3, index.lineLengths()[2]);
        assertEquals(4, index.lineLengths()[3]);
        assertEquals(3, index.lineLengths()[4]);
        assertEquals(4, index.lineLengths()[5]);
        assertEquals(1, index.lineLengths()[6]);
    }

    @Test
    void deleteWithinSingleLine() {
        var index = LineIndex.of(5);
        index.add("abcd\n".getBytes());
        index.delete(0, 1, 2);
        assertEquals(2, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);
        assertEquals(0, index.lineLengths()[1]);
    }

    @Test
    void deleteAcrossMultipleLines() {
        var index = LineIndex.of(5);
        index.add("ab\ncd\nef\ngh\n".getBytes());
        index.delete(0, 1, 6);
        assertEquals(3, index.lineLengths().length);
        assertEquals(3, index.lineLengths()[0]);
        assertEquals(3, index.lineLengths()[1]);
        assertEquals(0, index.lineLengths()[2]);
    }

    @Test
    void lines() {

        int[] ret = LineIndex.lines("".getBytes());
        assertEquals(0, ret.length);

        ret = LineIndex.lines("a".getBytes());
        assertEquals(1, ret.length);
        assertEquals(1, ret[0]);

        ret = LineIndex.lines("ab".getBytes());
        assertEquals(1, ret.length);
        assertEquals(2, ret[0]);

        ret = LineIndex.lines("ab\n".getBytes());
        assertEquals(2, ret.length);
        assertEquals(3, ret[0]);
        assertEquals(0, ret[1]);

        ret = LineIndex.lines("\n".getBytes());
        assertEquals(2, ret.length);
        assertEquals(1, ret[0]);
        assertEquals(0, ret[1]);

        ret = LineIndex.lines("\n\n".getBytes());
        assertEquals(3, ret.length);
        assertEquals(1, ret[0]);
        assertEquals(1, ret[1]);
        assertEquals(0, ret[2]);

        ret = LineIndex.lines("abc\r\nde\r\nf".getBytes());
        assertEquals(3, ret.length);
        assertEquals(5, ret[0]);
        assertEquals(4, ret[1]);
        assertEquals(1, ret[2]);
    }
}
