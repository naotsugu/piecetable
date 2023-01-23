package com.mammb.code.syntax.types;

import com.mammb.code.editor.Colors;
import com.mammb.code.editor.Utils;
import com.mammb.code.syntax.Highlighter;
import com.mammb.code.syntax.Token;
import com.mammb.code.syntax.Tokenizer;
import javafx.scene.paint.Paint;

public class Json implements Highlighter {

    private final Tokenizer tokenizer = new JsonTokenizer();

    @Override
    public Tokenizer tokenizer() {
        return tokenizer;
    }

    @Override
    public Paint colorOf(Token token) {
        return switch (token.name()) {
            case "string" -> Colors.blockCommentColor;
            case "key-string" -> Colors.kwColor;
            case "number" -> Colors.numberLiteralColor;
            case "null", "true", "false" -> Colors.yellowColor;
            default  -> Colors.fgColor;
        };
    }

    static class JsonTokenizer extends Tokenizer {
        @Override
        public Token next() {
            char ch = readChar();
            int pos = position();
            return switch (ch) {
                case ' ', '\t', '\n', '\r' -> token().with("sp", pos, 1);
                case '0','1','2','3','4','5','6','7','8','9','-' -> Helper.readNumber(this, "number");
                case '"' -> readString();
                case 't' -> Helper.readTrue(this);
                case 'f' -> Helper.readFalse(this);
                case 'n' -> Helper.readNull(this);
                case  0  -> token().empty(pos);
                //case '{', '[', ':', ',', ']', '}' -> token().itself(str[ch], pos);
                default  -> token().itself(Helper.str(ch), pos);
            };
        }

        public Token readString() {
            Token token = Helper.readString(this, "string", '"');
            int n = 0;
            for (;;) {
                char ch = peekChar(n++);
                if (Helper.isWhitespace(ch)) continue;
                return (ch == ':')
                    ? token.with("key-string", token.position(), token.length())
                    : token;
            }
        }

    }

}
