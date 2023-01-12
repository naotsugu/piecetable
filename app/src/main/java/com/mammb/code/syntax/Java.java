package com.mammb.code.syntax;

import com.mammb.code.trie.Range;
import com.mammb.code.trie.Trie;
import java.util.List;
import java.util.stream.Stream;

public class Java {

    private static final String keyword = """
    abstract continue for new switch assert default goto package synchronized boolean do if private
    this break double implements protected throw byte else import public throws case enum instanceof
    return transient catch extends int short try char final interface static void class finally long
    strictfp volatile const float native super while var record sealed with yield to transitive uses""";

    private static final Trie trie = new Trie();
    static {
        Stream.of(keyword.split("\s")).forEach(trie::put);
    }

    public static List<Range> keywordRangeOf(String text) {
        return trie.matchWords(text);
    }

}
