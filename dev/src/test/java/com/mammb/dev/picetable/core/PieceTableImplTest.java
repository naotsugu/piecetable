package com.mammb.dev.picetable.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PieceTableImplTest {

    @Test
    void insert() {
        var pieceTable = new PieceTableImpl();
        pieceTable.insert(0, "ac".getBytes());
        pieceTable.insert(1, "b".getBytes());
        assertEquals("abc", new String(pieceTable.bytes()));
    }
}
