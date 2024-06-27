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
        var index = LineIndex.of();
        index.add("ab\n\ncde\nf\ng".getBytes());
        assertEquals(0, index.get(0));  // |a|b|$|
        assertEquals(3, index.get(1));  // |$|
        assertEquals(4, index.get(2));  // |c|d|e|$|
        assertEquals(8, index.get(3));  // |f|$|
        assertEquals(10, index.get(4)); // |g|

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
