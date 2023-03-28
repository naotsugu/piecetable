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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link Trie).
 * @author Naotsugu Kobayashi
 */
class TrieTest {

    @Test
    void testSearch() {
        Trie trie = new Trie();
        trie.put("romane");
        trie.put("romanus");
        trie.put("romulus");
        trie.put("rubens");
        trie.put("ruber");
        trie.put("rubicon");

        assertTrue(trie.search("romane"));
        assertTrue(trie.search("romanus"));

        assertFalse(trie.search("roma"));
        assertFalse(trie.search("roman"));
        assertFalse(trie.search("apple"));
        assertFalse(trie.search("romules"));
        assertFalse(trie.search("rubicundus"));


        assertTrue(trie.startsWith("roma"));
        assertTrue(trie.startsWith("roman"));

        assertFalse(trie.startsWith("apple"));
        assertFalse(trie.startsWith("romules"));
        assertFalse(trie.startsWith("rubicundus"));


        trie.remove("romanus");
        trie.remove("romulus");

        assertTrue(trie.search("romane"));
        assertFalse(trie.search("romanus"));
        assertFalse(trie.search("romulus"));
    }

}
