package com.mammb.code.piecetable;

import org.junit.jupiter.api.Test;

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
        assertEquals("abc", table.toString());

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

}
