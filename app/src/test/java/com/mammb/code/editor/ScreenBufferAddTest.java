package com.mammb.code.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenBufferAddTest {


    @Test void testAdd1() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.prev(); sb.add("1");
        // 0: 1|aa
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            1aa""", sb.peekString(0, 3));
    }

    @Test void testAdd2() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("2");
        // 0: a2|a
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(2, sb.getCaretOffsetX());
        assertEquals("""
            a2a""", sb.peekString(0, 3));
    }

    @Test void testAdd3() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("3");
        // 0: aa3|
        assertEquals(1, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            aa3""", sb.peekString(0, 3));
    }

    @Test void testAdd4() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa\n"); sb.prev(); sb.add("4");
        // 0: aa4|
        // 1: \n
        assertEquals(2, sb.rows.size());
        assertEquals(0, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            aa4
            """, sb.peekString(0, 4));
    }

    @Test void testAddLfLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.prev(); sb.add("\n");
        // 0: \n
        // 1:|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""

            aa""", sb.peekString(0, 3));
    }

    @Test void testAddLfCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("\n");
        // 0: a\n
        // 1:|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            a
            a""", sb.peekString(0, 3));
    }

    @Test void testAddLfRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("\n");
        // 0: aa\n
        // 1:|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            aa
            """, sb.peekString(0, 3));
    }

    @Test void testAddMultiLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("b\nb");
        // 0: b\n
        // 1: b|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            b
            baa""", sb.peekString(0, 5));
    }

    @Test void testAddMultiCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("b\nb");
        // 0: ab\n
        // 1: b|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            ab
            ba""", sb.peekString(0, 5));
    }

    @Test void testAddMultiRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("b\nb");
        // 0: aab\n
        // 1: b|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            aab
            b""", sb.peekString(0, 5));
    }

    @Test void testAddHalfLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("b\n");
        // 0: b\n
        // 1:|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            b
            aa""", sb.peekString(0, 4));
    }

    @Test void testAddHalfCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("b\n");
        // 0: ab\n
        // 1:|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            ab
            a""", sb.peekString(0, 4));
    }

    @Test void testAddHalfRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("b\n");
        // 0: aab\n
        // 1:|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(0, sb.getCaretOffsetX());
        assertEquals("""
            aab
            """, sb.peekString(0, 4));
    }

    @Test void testAddHalf2Left() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("\nb");
        // 0: \n
        // 1: b|aa
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""

            baa""", sb.peekString(0, 4));
    }

    @Test void testAddHalf2Center() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("\nb");
        // 0: a\n
        // 1: b|a
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            a
            ba""", sb.peekString(0, 4));
    }

    @Test void testAddHalf2Right() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("\nb");
        // 0: aa\n
        // 1: b|
        assertEquals(2, sb.rows.size());
        assertEquals(1, sb.getCaretOffsetY());
        assertEquals(1, sb.getCaretOffsetX());
        assertEquals("""
            aa
            b""", sb.peekString(0, 4));
    }


    @Test void testAddLeft() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.home(); sb.add("b\ncc\nddd");
        // 0: b\n
        // 1: cc\n
        // 2: ddd|aa
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            b
            cc
            dddaa""", sb.peekString(0, 10));
    }

    @Test void testAddCenter() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.prev(); sb.add("b\ncc\nddd");
        // 0: ab\n
        // 1: cc\n
        // 2: ddd|a
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            ab
            cc
            ddda""", sb.peekString(0, 10));
    }

    @Test void testAddRight() {

        var sb = new ScreenBuffer();
        sb.setScreenRowSize(5);

        sb.add("aa"); sb.add("b\ncc\nddd");
        // 0: aab\n
        // 1: cc\n
        // 2: ddd|
        assertEquals(3, sb.rows.size());
        assertEquals(2, sb.getCaretOffsetY());
        assertEquals(3, sb.getCaretOffsetX());
        assertEquals("""
            aab
            cc
            ddd""", sb.peekString(0, 10));
    }

}
