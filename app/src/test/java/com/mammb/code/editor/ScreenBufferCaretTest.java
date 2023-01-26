package com.mammb.code.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScreenBufferCaretTest {

    @Test
    void testDelete1() {

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
}
