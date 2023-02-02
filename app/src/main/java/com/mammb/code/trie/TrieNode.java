package com.mammb.code.trie;

import java.util.*;
import java.util.stream.Collectors;

public class TrieNode {

    private final TrieNode parent;
    private final Map<Character, TrieNode> children = new HashMap<>();
    private char[] remaining = new char[0];
    private boolean endOfWord;


    private TrieNode() {
        parent = null;
    }


    TrieNode(TrieNode parent) {
        this.parent = Objects.requireNonNull(parent);
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

        if (remaining.length == 0 && children.isEmpty()) {
            remaining = text.substring(depth).toCharArray();
            return this;
        }

        if (remaining.length > 0) {
            assert children.isEmpty();
            String prev = text();
            remaining = new char[0];
            char ch = prev.charAt(depth());
            TrieNode node = children.computeIfAbsent(ch, c -> new TrieNode(this));
            node.put(prev);
        }
        char ch = text.charAt(depth());
        TrieNode node = children.computeIfAbsent(ch, c -> new TrieNode(this));
        node.put(text);

        return this;

    }


    public TrieNode search(String text) {
        int depth = depth();
        if (depth >= text.length()) {
            return this;
        }
        if (remaining.length > 0) {
            String str = new String(remaining);
            if (str.startsWith(text.substring(depth))) {
                return this;
            }
        } else {
            char ch = text.charAt(depth());
            if (children.containsKey(ch)) {
                return children.get(ch).search(text);
            }
        }
        return null;
    }


    void delete() {
        if (isRoot()) {
            return;
        }
        if (children.isEmpty()) {
            parent.delete(key());
        } else {
            endOfWord = false;
        }
    }

    private void delete(Character key) {
        if (key == null) {
            return;
        }
        children.remove(key);
        if (!isRoot() && children.isEmpty()) {
            parent.delete(key());

        }
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

    public int length() {
        return depth() + remaining.length + (children.isEmpty() ? 0 : 1);
    }

    private Character key() {
        if (isRoot()) {
            return null;
        }
        return parent.children.entrySet().stream()
            .filter(e -> e.getValue() == this)
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public String text() {
        return (remaining.length > 0)
            ? new String(concat(upstreamChars(), remaining))
            : new String(upstreamChars());
    }

    private char[] upstreamChars() {
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

    private char[] concat(char[] chars1, char[] chars2) {
        char[] ret = new char[chars1.length + chars2.length];
        System.arraycopy(chars1, 0, ret, 0, chars1.length);
        System.arraycopy(chars2, 0, ret, chars1.length, chars2.length);
        return ret;
    }

    @Override
    public String toString() {
        return isLeaf() ? text() : children.values().stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    }

}
