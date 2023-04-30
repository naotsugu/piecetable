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
package com.mammb.code.editor.syntax;

import java.util.*;

/**
 * TrieNode.
 * @author Naotsugu Kobayashi
 */
class TrieNode {

    /** The parent node. */
    private final TrieNode parent;

    /** The children. */
    private final Map<Integer, TrieNode> children;

    /** The marker of end of word. */
    private boolean endOfWord;


    /**
     * Constructor.
     * @param parent the parent node
     */
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
