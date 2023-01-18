package com.mammb.code.syntax;

import com.mammb.code.editor.Colors;
import com.mammb.code.editor.Strings;

import java.util.List;

public class Markdown implements Highlighter {

    @Override
    public List<PaintText> apply(int line, String text) {
        int sectionLevel = Strings.countToken('#', text);
        if (sectionLevel > 0) {
            return List.of(
                new PaintText(text.substring(0, sectionLevel), Colors.kwColor),
                new PaintText(text.substring(sectionLevel), Colors.fgColor));
        }
        return List.of(new PaintText(text, Colors.fgColor));
    }

    @Override
    public void invalidAfter(int line) {

    }
}
