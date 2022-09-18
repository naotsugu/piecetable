package com.mammb.code.piecetable.piece;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link CursoredList}.
 * @author Naotsugu Kobayashi
 */
class CursoredListTest {

    @Test
    void testCursorList() {

        var list = new CursoredList();

        list.add(0, new Piece(null, 0, 3));
        // |0|1|2|
        assertEquals(1, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(0, list.at(0).index());
        assertEquals(0, list.at(0).position());
        assertEquals(0, list.at(2).index());
        assertEquals(0, list.at(2).position());


        list.add(1, new Piece(null, 0, 2));
        // |0|1|2|  |3|4|
        assertEquals(2, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(2, list.get(1).length());
        assertEquals(0, list.at(2).index());
        assertEquals(0, list.at(2).position());
        assertEquals(1, list.at(3).index());
        assertEquals(3, list.at(3).position());
        assertEquals(1, list.at(4).index());
        assertEquals(3, list.at(4).position());


        list.add(2, new Piece(null, 0, 4));
        // |0|1|2|  |3|4|  |5|6|7|8|
        assertEquals(3, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(2, list.get(1).length());
        assertEquals(4, list.get(2).length());
        assertEquals(0, list.at(0).index());
        assertEquals(0, list.at(0).position());
        assertEquals(0, list.at(2).index());
        assertEquals(0, list.at(2).position());
        assertEquals(1, list.at(3).index());
        assertEquals(3, list.at(3).position());
        assertEquals(1, list.at(4).index());
        assertEquals(3, list.at(4).position());
        assertEquals(2, list.at(5).index());
        assertEquals(5, list.at(5).position());
        assertEquals(2, list.at(8).index());
        assertEquals(5, list.at(8).position());


        list.remove(1);
        // |0|1|2|  |3|4|5|6|
        assertEquals(2, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(4, list.get(1).length());
        assertEquals(1, list.at(3).index());
        assertEquals(3, list.at(3).position());
        assertEquals(1, list.at(6).index());
        assertEquals(3, list.at(6).position());

    }
}
