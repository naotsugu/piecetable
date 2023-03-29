package com.mammb.code.editor3.syntax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SourceTest {

    @Test
    void testReadChar() {
        var source = Source.of("abc");
        assertEquals('a', source.readChar());
        assertEquals('b', source.readChar());
        assertEquals('c', source.readChar());
        assertEquals(0, source.readChar());
    }

    @Test
    void testReadCharWide() {
        var source = Source.of("あい");
        assertEquals('あ', source.readChar());
        assertEquals('い', source.readChar());
        assertEquals(0, source.readChar());
    }

    @Test
    void testReadCharSurrogate() {
        var source = Source.of("😊");
        char h = source.readChar();
        char l = source.readChar();
        assertEquals("😊", new String(new char[] { h, l }));
        assertEquals(0, source.readChar());
    }

}
