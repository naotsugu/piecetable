package com.mammb.code.syntax.types;

import com.mammb.code.editor.Colors;
import com.mammb.code.syntax.Highlighter;
import com.mammb.code.syntax.Token;
import com.mammb.code.syntax.Tokenizer;
import javafx.scene.paint.Paint;

public class Markdown implements Highlighter {

    private final Tokenizer tokenizer = new MarkdownTokenizer();

    @Override
    public Tokenizer tokenizer() { return tokenizer; }

    @Override
    public Paint colorOf(Token token) {
        return switch (token.name()) {
            case "section" -> Colors.kwColor;
            default  -> Colors.fgColor;
        };
    }

    static class MarkdownTokenizer extends Tokenizer {

        @Override
        public Token next() {
            char ch = readChar();
            int pos = position();
            return switch (ch) {
                case  0  -> token().empty(pos);
                case '#' -> token().with("section", pos, 1);
                default  -> token().itself(Helper.str(ch), pos);
            };
        }
    }

}
