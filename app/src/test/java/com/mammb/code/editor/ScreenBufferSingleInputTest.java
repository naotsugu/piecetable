package com.mammb.code.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenBufferSingleInputTest {

    @Test void test() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa");
        // 0: aa|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals(2, sb.charCountOnScreen());

        sb.add("\n");
        // 0: aa\n
        // 1:│
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("\n");
        // 0: aa\n
        // 1: \n
        // 2:│
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.prevLine();
        // 0: aa\n
        // 1:│\n
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.prev();
        // 0: aa│\n
        // 1:\n
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());

        sb.delete(1);
        sb.next();
        // 0: aa\n
        // 1:│
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("b");sb.add("b");sb.add("b");
        // 0: aa\n
        // 1: bbb│
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());

        sb.home();
        // 0: aa\n
        // 1:│bbb
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.end();
        // 0: aa\n
        // 1: bbb│
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());

        sb.prev();sb.add("\n");
        // 0: aa\n
        // 1: bb\n
        // 2: │b
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("c");sb.add("c");sb.add("\n");sb.delete(1);
        // 0: aa\n
        // 1: bb\n
        // 2: cc\n
        // 3:│
        assertEquals(3, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            aa
            bb
            cc
            """, sb.peekString(0, 9));

        sb.prev(); sb.prev();
        // 0: aa\n
        // 1: bb\n
        // 2: c│c\n
        // 3:
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.next(); sb.prevLine();
        // 0: aa\n
        // 1: bb│\n
        // 2: cc\n
        // 3:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());

        sb.prevLine(); sb.delete(1);
        // 0: aa│bb\n
        // 1: cc\n
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals("""
            aabb
            cc
            """, sb.peekString(0, 8));

        sb.backSpace(); sb.backSpace(); sb.backSpace();
        // 0:│bb\n
        // 1: cc\n
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.next(); sb.nextLine();
        // 0: bb\n
        // 1: c│c\n
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.delete(1); sb.delete(1);
        // 0: bb\n
        // 1: c│
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.backSpace();
        // 0: bb\n
        // 1: │
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(3, sb.charCountOnScreen());
        assertEquals("""
            bb
            """, sb.peekString(0, 3));

        sb.backSpace(); sb.backSpace(); sb.backSpace();
        // 0:│
        // 1:
        // 2:
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals(0, sb.charCountOnScreen());

    }

    @Test void tests() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa\nb");
        // 0: aa\n
        // 1: b│
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.prev();
        // 0: aa\n
        // 1:│b
        // 2:
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("\n");
        // 0: aa\n
        // 1: \n
        // 2:│b
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.prev();
        // 0: aa\n
        // 1:│\n
        // 2: b
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());

        sb.add("b");
        // 0: aa\n
        // 1: b│\n
        // 2: b
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());

        sb.add("b");
        // 0: aa\n
        // 1: bb│\n
        // 2: b
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());

    }
}
