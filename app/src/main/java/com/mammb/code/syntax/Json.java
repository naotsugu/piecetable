package com.mammb.code.syntax;

import com.mammb.code.editor.Colors;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;

public class Json implements Highlighter {

    @Override
    public List<PaintText> apply(int line, String text) {
        List<PaintText> list = new ArrayList<>();
        Tokenizer tokenizer = tokenizer(text);
        int n = 0;
        for (;;) {
            Token token = tokenizer.next();
            if (token.length() == 0) break;
            Paint paint = switch (token.name()) {
                case "string" -> Colors.blockCommentColor;
                case "number" -> Colors.numColor;
                case "null", "true", "false" -> Colors.kwColor;
                default  -> Colors.fgColor;
            };
            if (token.position() > n) {
                list.add(new PaintText(text.substring(n, token.position()), Colors.fgColor));
                n = token.position();
            }
            list.add(new PaintText(text.substring(n, n + token.length()), paint));
            n = n + token.length();
        }
        if (n < text.length()) {
            list.add(new PaintText(text.substring(n), Colors.fgColor));
        }
        return list;
    }

    @Override
    public void invalidAfter(int line) {

    }


    private Tokenizer tokenizer(String text) {
        return new Tokenizer(text) {
            @Override
            Token next() {
                char ch = readNextChar();
                return switch (ch) {
                    case '"' -> readString();
                    case '{' -> new Token("{", position(), 1);
                    case '[' -> new Token("[", position(), 1);
                    case ':' -> new Token(":", position(), 1);
                    case ',' -> new Token(",", position(), 1);
                    case 't' -> readTrue();
                    case 'f' -> readFalse();
                    case 'n' -> readNull();
                    case ']' -> new Token("]", position(), 1);
                    case '}' -> new Token("}", position(), 1);
                    case '0','1','2','3','4','5','6','7','8','9','-' -> readNumber();
                    case  0  -> new Token("", position(), 0);
                    default  -> new Token(Character.toString(ch), position(), 1);
                };
            }

            private Token readTrue() {
                if (peekChar(0) == 'r' && peekChar(1) == 'u' && peekChar(2) == 'e') {
                    int pos = position();
                    return new Token(read(4), pos, 4);
                } else {
                    return new Token("t", position(), 1);
                }
            }

            private Token readFalse() {
                if (peekChar(0) == 'a' && peekChar(1) == 'l' && peekChar(2) == 's' && peekChar(3) == 'e') {
                    int pos = position();
                    return new Token(read(5), pos, 5);
                } else {
                    return new Token("f", position(), 1);
                }

            }

            private Token readNull() {
                if (peekChar(0) == 'u' && peekChar(1) == 'l' && peekChar(2) == 'l') {
                    int pos = position();
                    return new Token(read(4), pos, 4);
                } else {
                    return new Token("n", position(), 1);
                }
            }

            private Token readString() {
                int n = 0;
                for (;;) {
                    char ch = peekChar(n++);
                    if (ch < ' ' || ch == '"') {
                        int pos = position();
                        read(n + 1);
                        return new Token("string", pos, n + 1);
                    } else if (ch == '\\') {
                        n++;
                    }
                }
            }

            private Token readNumber()  {
                int n = 0;
                for (;;) {
                    char ch = peekChar(n++);
                    if (!isDigit(ch) && ch != '-' && ch != '.' && ch != 'e' && ch != 'E') {
                        int pos = position();
                        read(n + 1);
                        return new Token("number", pos, n + 1);
                    }
                }
            }
        };
    }

}
