package com.mammb.code.trie;

import java.util.*;
import java.util.stream.Collectors;

public class TrieNode {

    private final TrieNode parent;
    private final Map<Character, TrieNode> children;
    private List<String> tails;
    private boolean endOfWord;


    private TrieNode() {
        parent = null;
        children = new HashMap<>();
    }

    TrieNode(TrieNode parent) {
        this.parent = Objects.requireNonNull(parent);
        this.children = new HashMap<>();
    }

    public static TrieNode root() {
        return new TrieNode();
    }

    public TrieNode put(String text) {

        int depth = depth();

        if (depth >= text.length()) {
            endOfWord = true;
            return this;
        }

        if (depth > Trie.MAX_DEPTH) {
            tails = (tails == null) ? new ArrayList<>() : tails;
            tails.add(text.substring(depth));
            endOfWord = true;
        } else {
            char ch = text.charAt(depth());
            TrieNode node = children.computeIfAbsent(ch, c -> new TrieNode(this));
            node.put(text);
        }
        return this;
    }

    public TrieNode search(String text) {
        int depth = depth();
        if (depth >= text.length()) {
            return this;
        }
        if (children.isEmpty() && !tails.isEmpty()) {
            for (String tail : tails) {
                if (text.substring(depth).equals(tail)) {
                    return this;
                }
            }
        } else {
            char ch = text.charAt(depth());
            if (children.containsKey(ch)) {
                return children.get(ch).search(text);
            }
        }
        return null;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isEndOfWord() {
        return endOfWord;
    }

    public int depth() {
        int depth = 0;
        TrieNode node = this;
        while (!node.isRoot()) {
            node = node.parent;
            depth++;
        }
        return depth;
    }

    private Character key() {
        if (isRoot()) {
            return null;
        }
        return parent.children.entrySet().stream()
            .filter(e -> e.getValue().equals(this))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public String text() {
        if (tails == null) {
            return new String(chars());
        } else {
            List<String> ret = new ArrayList<>(tails.size());
            for (String tail : tails) {
                char[] parentChars = chars();
                char[] tailChars = tail.toCharArray();
                char[] concat = new char[parentChars.length + tailChars.length];
                System.arraycopy(parentChars, 0, concat, 0, parentChars.length);
                System.arraycopy(tailChars, 0, concat, parentChars.length, tailChars.length);
                ret.add(new String(concat));
            }
            return String.join("|", ret);
        }
    }

    private char[] chars() {
        int len = depth();
        char[] chars = new char[len];
        TrieNode node = this;
        while (!node.isRoot()) {
            Character ch = node.key();
            if (ch == null) throw new IllegalStateException();
            chars[--len] = ch;
            node = node.parent;
        }
        return chars;
    }

    @Override
    public String toString() {
        return isLeaf() ? text() : children.values().stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    }

}
