package com.mammb.code.piecetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PieceTableTest {

    @Test
    void testInsert() {

        var pt = PieceTable.of("abc");
        assertEquals("abc", pt.toString());

        pt.insert(3, "def");
        assertEquals("abcdef", pt.toString());

        pt.insert(6, "ghi");
        assertEquals("abcdefghi", pt.toString());
        pt.dump();
        pt.delete(2, 2);
        assertEquals("abefghi", pt.toString());

        pt.insert(0, "**");
        assertEquals("**abefghi", pt.toString());

        pt.insert(9, "**");
        assertEquals("**abefghi**", pt.toString());
    }
}
