package com.mammb.code.piecetable.examples;

import com.mammb.code.piecetable.examples.Style.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public interface Syntax {
    String name();
    List<StyleSpan> apply(int row, String text);

    record PassThrough(String name) implements Syntax {
        @Override public List<StyleSpan> apply(int row, String text) {
            return List.of();
        }
    }

    static Syntax of(String name) {
        return switch (name.toLowerCase()) {
            case "java" -> new JavaSyntax();
            default -> new PassThrough(name);
        };
    }
    record Anchor(int row, int col) implements Comparable<Anchor> {
        @Override
        public int compareTo(Anchor that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }
    enum Palette {
        DEEP_GREEN("#6A8759"),
        ORANGE("#CC7832"),
        ;
        Palette(String colorString) {
            this.colorString = colorString;
        }
        final String colorString;
    }

    static int read(Trie keywords, String colorString, String text, int offset, List<StyleSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            } else {
                break;
            }
        }
        if (!sb.isEmpty() && keywords.match(sb.toString())) {
            spans.add(new StyleSpan(
                new TextColor(colorString), offset, sb.length()));
        }
        return offset + sb.length();
    }

    static int read(char to, char esc, String colorString, String text, int offset, List<StyleSpan> spans) {
        for (int i = offset + 1; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == esc) continue;
            if (ch == to) {
                spans.add(new StyleSpan(
                    new TextColor(colorString), offset, i - offset + 1));
                return i;
            }
        }
        return offset;
    }

    class JavaSyntax implements Syntax {

        private final Trie keywords = new Trie();
        {
            Stream.of("""
        abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,
        this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,
        return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,
        strictfp,volatile,const,float,native,super,while,var,record,sealed,with,yield,to,transitive,uses
        """.split("[,\\s]")).forEach(keywords::put);
        }
        private final TreeMap<Anchor, String> scopes = new TreeMap<>();

        @Override
        public String name() {
            return "java";
        }

        @Override
        public List<StyleSpan> apply(int row, String text) {
            if (text == null || text.isBlank()) {
                return Collections.emptyList();
            }
            scopes.tailMap(new Anchor(row, 0), true).clear();
            boolean inBlockComment = false;
            for (var e : scopes.entrySet()) {
                if (inBlockComment && e.getValue().equals("*/")) {
                    inBlockComment = false;
                } else if (!inBlockComment && e.getValue().equals("/*")) {
                    inBlockComment = true;
                }
            }
            List<StyleSpan> spans = new ArrayList<>();
            int i = 0;

            if (inBlockComment && !text.contains("*/")) {
                spans.add(new StyleSpan(
                    new TextColor(Palette.DEEP_GREEN.colorString), 0, text.length()));
                return spans;
            }

            if (inBlockComment && text.contains("*/")) {
                i = text.indexOf("*/") + 2;
                scopes.put(new Anchor(row, i), "*/");
                spans.add(new StyleSpan(
                    new TextColor(Palette.DEEP_GREEN.colorString), 0, i));
            }

            for (; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                    scopes.put(new Anchor(row, i), "/*");
                    int e = text.substring(i).indexOf("*/");
                    if (e > 0) {
                        e += 2;
                        spans.add(new StyleSpan(
                            new TextColor(Palette.DEEP_GREEN.colorString), i, i + e));
                        scopes.put(new Anchor(row, i + e), "*/");
                        i += e;
                    } else {
                        spans.add(new StyleSpan(
                            new TextColor(Palette.DEEP_GREEN.colorString), i, text.length()));
                        return spans;
                    }
                } else if (ch == '"') {
                    i = Syntax.read('"', '\\', Palette.DEEP_GREEN.colorString, text, i, spans);
                } else if (Character.isAlphabetic(ch)) {
                    i = Syntax.read(keywords, Palette.ORANGE.colorString, text, i, spans);
                }
            }
            return spans;
        }

    }


    class Trie {
        private final TrieNode root;
        public Trie() {
            root = new TrieNode(null);
        }
        public void put(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length();) {
                int cp = word.codePointAt(i);
                node = node.createIfAbsent(cp);
                i += Character.charCount(cp);
            }
            node.setEndOfWord();
        }
        public void remove(String word) {
            TrieNode node = searchPrefix(word);
            if (node == null || !node.isEndOfWord()) {
                return;
            }
            node.setEndOfWord(false);
            node.removeIfEmpty();
        }
        public boolean match(String word) {
            TrieNode node = searchPrefix(word);
            return node != null && node.isEndOfWord();
        }
        public boolean startsWith(String prefix) {
            return searchPrefix(prefix) != null;
        }
        public List<String> suggestion(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length();) {
                int cp = word.codePointAt(i);
                if (node.contains(cp)) {
                    node = node.get(cp);
                } else {
                    break;
                }
                i += Character.charCount(cp);
            }
            return node.childKeys();
        }
        private TrieNode searchPrefix(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length();) {
                int cp = word.codePointAt(i);
                if (node.contains(cp)) {
                    node = node.get(cp);
                } else {
                    return null;
                }
                i += Character.charCount(cp);
            }
            return node;
        }
        private static class TrieNode {
            private final TrieNode parent;
            private final Map<Integer, TrieNode> children;
            private boolean endOfWord;
            TrieNode(TrieNode parent) {
                this.parent = parent;
                this.children = new HashMap<>();
            }
            TrieNode createIfAbsent(Integer key) {
                return children.computeIfAbsent(key, k -> new TrieNode(this));
            }
            boolean contains(int codePoint) {
                return children.containsKey(codePoint);
            }
            TrieNode get(int codePoint) {
                return children.get(codePoint);
            }
            void put(int codePoint, TrieNode node) {
                children.put(codePoint, node);
            }
            void removeIfEmpty() {
                if (parent == null) {
                    return;
                }
                if (children.isEmpty()) {
                    parent.children.remove(key());
                    parent.removeIfEmpty();
                }
            }
            private Integer key() {
                if (parent == null) {
                    return null;
                }
                return parent.children.entrySet().stream()
                    .filter(e -> e.getValue() == this)
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElse(null);
            }
            List<String> childKeys() {
                return children.keySet().stream().map(Character::toString).toList();
            }
            void setEndOfWord() { endOfWord = true; }
            void setEndOfWord(boolean endOfWord) { this.endOfWord = endOfWord; }
            boolean isEndOfWord() { return endOfWord; }
        }
    }

}
