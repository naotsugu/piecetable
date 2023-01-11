package com.mammb.code.trie;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    @Test
    void testMatch() {

        var trie = new Trie();
        trie.put("public");
        trie.put("private");
        trie.put("protected");

        assertEquals(true, trie.match("public"));
        assertEquals(true, trie.fullMatch("public"));

        assertEquals(true, trie.match("private"));
        assertEquals(true, trie.fullMatch("private"));

        assertEquals(true, trie.match("protected"));
        assertEquals(true, trie.fullMatch("private"));

        assertEquals(true, trie.match("publi"));
        assertEquals(false, trie.fullMatch("publi"));

        assertEquals(false, trie.match("publix"));

    }

}
