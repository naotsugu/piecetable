package com.mammb.code.syntax.types;

import com.mammb.code.syntax.Token;
import com.mammb.code.syntax.Tokenizer;
import java.util.stream.IntStream;

public class Helper {

    private static final String[] str = IntStream.range(0, 128).mapToObj(Character::toString).toArray(String[]::new);

    public static String str(char ch) {
        return (0 < ch && ch < 128) ? str[ch] : " ";
    }

    public static Token readString(Tokenizer tn, String name, char m) {
        int n = 0;
        for (;;) {
            char ch = tn.peekChar(n++);
            if (ch < ' ' || ch == m) {
                return tn.token().with(name, tn.consume(n + 1), n + 1);
            } else if (ch == '\\') {
                n++;
            }
        }
    }

    public static Token stdLineComment(Tokenizer tn, String name, char m) {
        if (tn.peekChar(0) == m) {
            int pos = tn.position();
            int index = tn.input().indexOf('\n', pos);
            int len = (index > -1) ? index - pos + 1 : tn.input().length() - pos;
            return tn.token().with(name, tn.consume(len), len);
        } else {
            return tn.token().itself(str(m), tn.position());
        }
    }


    public static Token readChar(Tokenizer tn, String name, char m) {
        int pos = tn.position();
        if (tn.peekChar(1) == m) {
            return tn.token().with(name, pos, 3);
        } else if (tn.peekChar(0) == '\\' && tn.peekChar(2) == m) {
            return tn.token().with(name, pos, 4);
        } else {
            return tn.token().itself(str(m), pos);
        }
    }

    public static Token readTrue(Tokenizer tn) {
        if (tn.peekChar(0) == 'r' && tn.peekChar(1) == 'u' && tn.peekChar(2) == 'e') {
            return tn.token().with("true", tn.consume(4), 4);
        } else {
            return tn.token().itself("t", tn.position());
        }
    }

    public static Token readFalse(Tokenizer tn) {
        if (tn.peekChar(0) == 'a' && tn.peekChar(1) == 'l' && tn.peekChar(2) == 's' && tn.peekChar(3) == 'e') {
            return tn.token().with("false", tn.consume(5), 5);
        } else {
            return tn.token().itself("f", tn.position());
        }
    }

    public static Token readNull(Tokenizer tn) {
        if (tn.peekChar(0) == 'u' && tn.peekChar(1) == 'l' && tn.peekChar(2) == 'l') {
            return tn.token().with("null", tn.consume(4), 4);
        } else {
            return tn.token().itself("n", tn.position());
        }
    }

    public static Token readNumber(Tokenizer tn, String name) {

        int pos = tn.position();
        final char head = tn.readAgain();
        char ch = head;
        int n = 0;

        if (ch == '-') {
            ch = tn.peekChar(n++);
            if (ch < '0' || ch > '9') return tn.token().itself(str(head), pos);
        }

        if (ch == '0') {
            ch = tn.peekChar(n++);
        } else {
            do {
                ch = tn.peekChar(n++);
            } while (ch >= '0' && ch <= '9');
        }

        if (ch == '.') {
            int count = 0;
            do {
                ch = tn.peekChar(n++);
                count++;
            } while (ch >= '0' && ch <= '9');
            if (count == 1) return tn.token().itself(str(head), pos);
        }

        if (ch == 'e' || ch == 'E') {
            ch = tn.peekChar(n++);
            if (ch == '+' || ch == '-') {
                ch = tn.peekChar(n++);
            }
            int count;
            for (count = 0; ch >= '0' && ch <= '9'; count++) {
                ch = tn.peekChar(n++);
            }
            if (count == 0) return tn.token().itself(str(head), pos);
        }
        return tn.token().with(name, tn.consume(n), n);
    }

    public static boolean isLetter(char c) { return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_'; }
    public static boolean isDigit(char c) { return '0' <= c && c <= '9'; }
    public static boolean isWhitespace(char c) { return c == ' ' || c == '\t' || c == '\n' || c == '\r'; }

}
