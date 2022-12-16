package com.mammb.code.editor;

import com.mammb.code.editor.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringsTest {

    @Test
    void splitLine() {

        var ret0 = Strings.splitLine("ab");
        assertEquals(1, ret0.length);
        assertEquals("ab", ret0[0]);

        var ret1 = Strings.splitLine("");
        assertEquals(1, ret1.length);
        assertEquals("", ret1[0]);

        var ret2 = Strings.splitLine("ab\ncd");
        assertEquals(2, ret2.length);
        assertEquals("ab\n", ret2[0]);
        assertEquals("cd", ret2[1]);

        var ret3 = Strings.splitLine("ab\n");
        assertEquals(2, ret3.length);
        assertEquals("ab\n", ret3[0]);
        assertEquals("", ret3[1]);

        var ret4 = Strings.splitLine("\ncd");
        assertEquals(2, ret4.length);
        assertEquals("\n", ret4[0]);
        assertEquals("cd", ret4[1]);

        var ret5 = Strings.splitLine("ab\ncd\nef\n");
        assertEquals(4, ret5.length);
        assertEquals("ab\n", ret5[0]);
        assertEquals("cd\n", ret5[1]);
        assertEquals("ef\n", ret5[2]);
        assertEquals("", ret5[3]);
    }
}
