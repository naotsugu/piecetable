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
package com.mammb.code.trie;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    @Test
    void testPartialMatch() {

        var trie = new Trie();
        trie.put("public");
        trie.put("private");
        trie.put("protected");

        assertEquals(true, trie.partialMatch("public"));
        assertEquals(true, trie.partialMatch("private"));
        assertEquals(true, trie.partialMatch("protected"));

        assertEquals(false, trie.partialMatch("apublic"));
        assertEquals(false, trie.partialMatch("pubalic"));
        assertEquals(false, trie.partialMatch("publica"));
        assertEquals(false, trie.partialMatch("ublic"));
        assertEquals(false, trie.partialMatch("pubic"));
        assertEquals(true,  trie.partialMatch("publi"));

        assertEquals(false, trie.partialMatch("aprotected"));
        assertEquals(false, trie.partialMatch("protaected"));
        assertEquals(false, trie.partialMatch("protecteda"));
        assertEquals(false, trie.partialMatch("rotected"));
        assertEquals(false, trie.partialMatch("proected"));
        assertEquals(true,  trie.partialMatch("protecte"));

        assertEquals(true, trie.partialMatch("publi"));
        assertEquals(true, trie.partialMatch("publ"));
        assertEquals(true, trie.partialMatch("pub"));
        assertEquals(true, trie.partialMatch("pu"));
        assertEquals(true, trie.partialMatch("p"));
        assertEquals(true, trie.partialMatch(""));

    }

    @Test
    void testMatch() {

        var trie = new Trie();
        trie.put("public");
        trie.put("private");
        trie.put("protected");

        assertEquals(true, trie.match("public"));
        assertEquals(true, trie.match("private"));
        assertEquals(true, trie.match("protected"));

        assertEquals(false, trie.match("apublic"));
        assertEquals(false, trie.match("pubalic"));
        assertEquals(false, trie.match("publica"));
        assertEquals(false, trie.match("ublic"));
        assertEquals(false, trie.match("pubic"));
        assertEquals(false,  trie.match("publi"));

        assertEquals(false, trie.match("aprotected"));
        assertEquals(false, trie.match("protaected"));
        assertEquals(false, trie.match("protecteda"));
        assertEquals(false, trie.match("rotected"));
        assertEquals(false, trie.match("proected"));
        assertEquals(false,  trie.match("protecte"));

        assertEquals(false, trie.match("publi"));
        assertEquals(false, trie.match("publ"));
        assertEquals(false, trie.match("pub"));
        assertEquals(false, trie.match("pu"));
        assertEquals(false, trie.match("p"));
        assertEquals(false, trie.match(""));

    }

    @Test
    void testMatchWords() {
        var trie = new Trie();
        trie.put("public");
        trie.put("private");
        trie.put("protected");
        trie.put("static");
        trie.put("void");

        List<Range> ranges = trie.matchWords("public static void main(String.. args) {");

        // public static void main(String.. args) {
        // 0123456789
        //           0123456789
        //                     0123456789

        assertEquals(3, ranges.size());

        assertEquals(0, ranges.get(0).start());
        assertEquals(6, ranges.get(0).endExclusive());

        assertEquals(7, ranges.get(1).start());
        assertEquals(13, ranges.get(1).endExclusive());

        assertEquals(14, ranges.get(2).start());
        assertEquals(18, ranges.get(2).endExclusive());

    }

    @Test
    void testDelete() {
        var trie = new Trie();
        trie.put("public");
        trie.put("protected");
        trie.put("void");

        assertEquals(true, trie.match("public"));
        assertEquals(true, trie.match("protected"));
        assertEquals(true, trie.match("void"));

        trie.delete("protected");
        assertEquals(true, trie.match("public"));
        assertEquals(false, trie.match("protected"));
        assertEquals(true, trie.match("void"));

        trie.delete("public");
        assertEquals(false, trie.match("public"));
        assertEquals(false, trie.match("protected"));
        assertEquals(true, trie.match("void"));

        trie.delete("void");
        assertEquals(false, trie.match("public"));
        assertEquals(false, trie.match("protected"));
        assertEquals(false, trie.match("void"));

        trie.put("public");
        trie.put("protected");
        trie.put("void");
        assertEquals(true, trie.match("public"));
        assertEquals(true, trie.match("protected"));
        assertEquals(true, trie.match("void"));

    }

}
