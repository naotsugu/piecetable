package com.mammb.code.trie;

import java.util.ArrayList;
import java.util.List;

public class Trie {

    private final TrieNode root;

    public Trie() {
        this.root = TrieNode.root();
    }

    public void put(String text) {
        root.put(text);
    }

    public boolean partialMatch(String word) {
        TrieNode ret = root.search(word);
        return ret != null;
    }

    public boolean match(String word) {
        TrieNode ret = root.search(word);
        return ret != null && (ret.isEndOfWord() || ret.length() == word.length());
    }

    public List<Range> matchWords(String text) {
        List<Range> ranges = new ArrayList<>();
        int offset = 0;
        boolean skip = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean isLetterOrDigit = Character.isLetterOrDigit(ch);
            if (i != 0 && !isLetterOrDigit && !skip) {
                if (match(text.substring(offset, i))) {
                    ranges.add(new Range(offset, i));
                }
            }
            if (!isLetterOrDigit) {
                offset = i + 1;
            }
            skip = !isLetterOrDigit;
        }
        return ranges;
    }

    @Override
    public String toString() {
        return root.toString();
    }

}
