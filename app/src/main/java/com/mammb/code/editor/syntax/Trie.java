/*
 * Copyright 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.syntax;

import java.util.List;

/**
 * Trie.
 * @author Naotsugu Kobayashi
 */
public class Trie {

    /** The node. */
    private final TrieNode root;


    /**
     * Constructor.
     */
    public Trie() {
        root = new TrieNode(null);
    }


    /**
     * Put the word.
     * @param word the word
     */
    public void put(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length();) {
            int cp = word.codePointAt(i);
            node = node.createIfAbsent(cp);
            i += Character.charCount(cp);
        }
        node.setEndOfWord();
    }


    /**
     * Remove the word.
     * @param word the word
     */
    public void remove(String word) {
        TrieNode node = searchPrefix(word);
        if (node == null || !node.isEndOfWord()) {
            return;
        }
        node.setEndOfWord(false);
        node.removeIfEmpty();
    }


    /**
     * Gets whether the specified word matches.
     * @param word the words to be inspected
     * @return {@code true}, if the specified word matches
     */
    public boolean match(String word) {
        TrieNode node = searchPrefix(word);
        return node != null && node.isEndOfWord();
    }


    /**
     * Gets whether the specified word left-hand matches.
     * @param prefix the text to be inspected
     * @return {@code true}, if the specified word left-hand matches
     */
    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }


    /**
     * Search the TrieNode.
     * @param word the specified word
     * @return the found TrieNode
     */
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


    /**
     * Gets the list of suggestion by the specified word.
     * @param word the specified word
     * @return the specified word
     */
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

}
