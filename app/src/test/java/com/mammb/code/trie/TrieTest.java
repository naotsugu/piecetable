package com.mammb.code.trie;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    @Test
    void test() {

        var trie = new Trie();
        trie.put("public");
        trie.put("private");
        trie.put("protected");

        assertEquals("public", trie.search("public").text());
        assertEquals("private", trie.search("private").text());
        assertEquals("protected", trie.search("protected").text());
    }

}
