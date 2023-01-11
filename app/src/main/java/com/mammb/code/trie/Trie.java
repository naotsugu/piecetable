package com.mammb.code.trie;

import java.util.ArrayList;
import java.util.List;

public class Trie {

    static final int MAX_DEPTH = 5;

    private final TrieNode root;

    public Trie() {
        this.root = TrieNode.root();
    }

    public void put(String text) {
        root.put(text);
    }

    public List<Range> search(String text) {
        return new ArrayList<>();
    }

    public boolean match(String text) {
        TrieNode ret = root.search(text);
        return ret != null;
    }

    public boolean fullMatch(String text) {
        TrieNode ret = root.search(text);
        return ret != null && ret.isEndOfWord();
    }

    @Override
    public String toString() {
        return root.toString();
    }

}
