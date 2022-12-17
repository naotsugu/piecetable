package com.mammb.code.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringsTest {

    @Test
    void splitLine() {

        var ret = Strings.splitLine("ab");
        assertEquals(1, ret.length);
        assertEquals("ab", ret[0]);

        ret = Strings.splitLine("");
        assertEquals(1, ret.length);
        assertEquals("", ret[0]);

        ret = Strings.splitLine("ab\ncd");
        assertEquals(2, ret.length);
        assertEquals("ab\n", ret[0]);
        assertEquals("cd", ret[1]);

        ret = Strings.splitLine("ab\n");
        assertEquals(2, ret.length);
        assertEquals("ab\n", ret[0]);
        assertEquals("", ret[1]);

        ret = Strings.splitLine("\ncd");
        assertEquals(2, ret.length);
        assertEquals("\n", ret[0]);
        assertEquals("cd", ret[1]);

        ret = Strings.splitLine("ab\ncd\nef\n");
        assertEquals(4, ret.length);
        assertEquals("ab\n", ret[0]);
        assertEquals("cd\n", ret[1]);
        assertEquals("ef\n", ret[2]);
        assertEquals("", ret[3]);

        ret = Strings.splitLine("\n");
        assertEquals(2, ret.length);
        assertEquals("\n", ret[0]);
        assertEquals("", ret[1]);
    }
}
