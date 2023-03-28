/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.syntax;

import java.util.List;

/**
 * Trie.
 * @author Naotsugu Kobayashi
 */
public class Trie {

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

    public boolean search(String word) {
        TrieNode node = searchPrefix(word);
        return node != null && node.isEndOfWord();
    }

    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
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
