package com.mammb.code.syntax;

import com.mammb.code.editor.Colors;
import com.mammb.code.syntax.types.*;
import javafx.scene.paint.Paint;
import java.util.ArrayList;
import java.util.List;

public interface Highlighter {

    record PaintText(String text, Paint paint) { }

    Tokenizer tokenizer();


    static Highlighter of(String name) {
        return switch (name) {
            case "java" -> new Javax();
            case "json" -> new Json();
            case "md"   -> new Markdown();
            default     -> new PassThrough();
        };
    }

    default Paint colorOf(Token token) { return Colors.fgColor; }
    default boolean canMarge(Token token, Token next) {
        return token.adjoining(next) && (next.name().equals("sp")
            || colorOf(token) == colorOf(next));
    }
    default void invalidAfter(int line) { }

    default List<PaintText> apply(int line, String text) {
        List<PaintText> list = new ArrayList<>();
        tokenizer().init(text);
        Token token = null;
        for (;;) {
            Token next = decorate(line, tokenizer().next());
            if (next.length() == 0) {
                if (token != null) {
                    list.add(consumeToken(token));
                }
                tokenizer().back(next);
                break;
            }
            if (token == null) {
                token = next;
                continue;
            }
            if (canMarge(token, next)) {
                token.marge(next);
                tokenizer().back(next);
                continue;
            }
            list.add(consumeToken(token));
            token = next;
        }
        return list;
    }

    default Token decorate(int line, Token token) {
        return token;
    }

    private PaintText consumeToken(Token token) {
        PaintText paintText = new PaintText(
            tokenizer().input().substring(token.position(), token.position() + token.length()),
            colorOf(token));
        tokenizer().back(token);
        return paintText;
    }

}
