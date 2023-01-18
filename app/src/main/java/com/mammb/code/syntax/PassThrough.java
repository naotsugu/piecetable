package com.mammb.code.syntax;

import com.mammb.code.editor.Colors;
import java.util.List;

class PassThrough implements Highlighter {

    @Override
    public List<PaintText> apply(int line, String text) {
        return List.of(new PaintText(text, Colors.fgColor));
    }

    @Override
    public void invalidAfter(int line) { }

}
