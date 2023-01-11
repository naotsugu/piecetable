package com.mammb.code.trie;

public class Trie {

    static final int MAX_DEPTH = 4;

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode(null);
    }

    public void put(String text) {
        root.put(text);
    }

    public TrieNode search(String text) {
        TrieNode current = root;
        return root.search(text);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("root=" + root);
        return sb.toString();
    }

}
