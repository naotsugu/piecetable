package com.mammb.code.syntax;

import com.mammb.code.editor.Colors;
import com.mammb.code.trie.Range;
import com.mammb.code.trie.Trie;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class Java implements Highlighter {

    private static final String keyword = """
    abstract continue for new switch assert default goto package synchronized boolean do if private
    this break double implements protected throw byte else import public throws case enum instanceof
    return transient catch extends int short try char final interface static void class finally long
    strictfp volatile const float native super while var record sealed with yield to transitive uses""";

    private static final Trie trie = new Trie();
    static {
        Stream.of(keyword.split("\s")).forEach(trie::put);
    }

    @Override
    public List<PaintText> asPaintRange(String text) {
        List<PaintText> list = new ArrayList<>();
        int offset = 0;
        for (Range range : trie.matchWords(text)) {
            if (offset != range.start()) {
                list.add(new PaintText(text.substring(offset, range.start()), Colors.fgColor));
            }
            list.add(new PaintText(text.substring(range.start(), range.endExclusive()), Colors.kwColor));
            offset = range.endExclusive();
        }
        if (offset < text.length()) {
            list.add(new PaintText(text.substring(offset), Colors.fgColor));
        }
        return list;
    }
}
