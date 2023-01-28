package com.mammb.code.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScreenBufferCaretTest {

    @Test
    void testCaretOrigin() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.prev();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.next();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.home();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.end();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.prevLine();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.nextLine();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.delete(1);
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

        sb.backSpace();
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.getCaretIndex());

    }

    @Test
    void testCaretScroll() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);
        assertEquals(5, sb.getScreenRowSize());

        sb.add("a\n");
        sb.add("a\n");
        sb.add("a\n");
        sb.add("a\n");
        // __________
        // 0: a$   1
        // 1: a$   2
        // 2: a$   3
        // 3: a$   4
        // 4:|     5
        // ----------
        assertEquals(0, sb.getOriginRowIndex());
        assertEquals(0, sb.getOriginIndex());
        assertEquals(5, sb.getTotalLines());
        assertEquals(5, sb.rows.size());
        assertEquals(4, sb.getCaretOffsetY());

        sb.add("a\n");
        // 0: a$   1
        // 1: a$   2
        // __________
        // 2: a$   3
        // 3: a$   4
        // 4: a$   5
        // 5:|     6
        //
        // ----------
        assertEquals(2, sb.getOriginRowIndex());
        assertEquals(4, sb.getOriginIndex());
        assertEquals(6, sb.getTotalLines());
        assertEquals(4, sb.rows.size());
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffset());

        sb.add("a\n");
        // 0: a$   1
        // 1: a$   2
        // __________
        // 2: a$   3
        // 3: a$   4
        // 4: a$   5
        // 5: a$   6
        // 6:|
        // ----------
        assertEquals(2, sb.getOriginRowIndex());
        assertEquals(4, sb.getOriginIndex());
        assertEquals(7, sb.getTotalLines());
        assertEquals(5, sb.rows.size());
        assertEquals(4, sb.getCaretOffsetY());
        assertEquals(8, sb.getCaretOffset());

        sb.prevLine();
        // 0: a$   1
        // 1: a$   2
        // __________
        // 2: a$   3
        // 3: a$   4
        // 4: a$   5
        // 5:|a$   6
        // 6:
        // ----------
        assertEquals(2, sb.getOriginRowIndex());
        assertEquals(4, sb.getOriginIndex());
        assertEquals(7, sb.getTotalLines());
        assertEquals(5, sb.rows.size());
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(6, sb.getCaretOffset());

    }

}
