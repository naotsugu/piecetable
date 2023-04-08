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
package com.mammb.code.editor3.syntax.type;

import com.mammb.code.editor3.model.Coloring;
import com.mammb.code.editor3.syntax.ColoringTo;
import com.mammb.code.editor3.syntax.Lexer;
import com.mammb.code.editor3.syntax.LexerSource;
import com.mammb.code.editor3.syntax.ScopeType;
import com.mammb.code.editor3.syntax.Token;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public class JsonLexer implements Lexer, ColoringTo {

    /** Token type. */
    protected interface Type extends TokenType {
        int KEY = TokenType.serial.getAndIncrement();
        int TEXT = TokenType.serial.getAndIncrement();
        int NUMBER = TokenType.serial.getAndIncrement();
        int LITERAL = TokenType.serial.getAndIncrement();
        int LINE_COMMENT = TokenType.serial.getAndIncrement();
        int COMMENT = TokenType.serial.getAndIncrement();
    }

    /** The input string. */
    private LexerSource source;


    /**
     * Constructor.
     * @param source the {@link LexerSource}
     */
    private JsonLexer(LexerSource source) {
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @param source the {@link LexerSource}
     * @return a lexer
     */
    public static Lexer of(LexerSource source) {
        return new JsonLexer(source);
    }


    /**
     * Create a new lexer.
     * @return a lexer
     */
    public static Lexer of() {
        return new JsonLexer(null);
    }


    @Override
    public void setSource(LexerSource source) {
        this.source = source;
    }


    @Override
    public Token nextToken() {

        if (source == null) return new Token(Type.EMPTY, ScopeType.NEUTRAL, 0, 0);

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> new Token(Type.SP, ScopeType.NEUTRAL, source.position(), 1);
            case '\n', '\r' -> lineEnd(source);
            case '/' -> readComment(source);
            case '"' -> readString(source);
            case 't' -> readTrue(source);
            case 'f' -> readFalse(source);
            case 'n' -> readNull(source);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' -> readNumber(source);
            case 0 -> new Token(Type.EMPTY, ScopeType.NEUTRAL, 0, 0);
            default -> new Token(Type.ANY, ScopeType.NEUTRAL, source.position(), 1);
        };
    }


    /**
     * Read comment.
     * @param source the lexer source
     * @return the token
     */
    private Token readComment(LexerSource source) {
        int pos = source.position();
        char ch = source.peekChar();
        if (ch == '/') {
            source.commitPeek();
            return new Token(Type.LINE_COMMENT, ScopeType.INLINE_START, pos, 2);
        } else if (ch == '*') {
            source.commitPeek();
            return new Token(JavaLexer.Type.COMMENT, ScopeType.BLOCK_START, pos, 2);
        } else {
            return new Token(Type.ANY, ScopeType.NEUTRAL, pos, 1);
        }
    }


    /**
     * Read the string.
     * @param source the lexer source
     * @return the token
     */
    private Token readString(LexerSource source) {
        int pos = source.position();
        char prev = 0;
        for (;;) {
            char ch = source.peekChar();
            if (ch < ' ' || (prev != '\\' && ch == '"')) {
                source.commitPeek();
                if (peekNextChar(source) == ':') {
                    return new Token(Type.KEY, ScopeType.NEUTRAL, pos, source.position() + 1 - pos);
                } else {
                    return new Token(Type.TEXT, ScopeType.NEUTRAL, pos, source.position() + 1 - pos);
                }
            }
            prev = ch;
        }
    }


    /**
     * Read the number.
     * @param source the lexer source
     * @return the token
     */
    private Token readNumber(LexerSource source) {

        int pos = source.position();
        char ch = source.currentChar();

        if (ch == '-') {
            ch = source.peekChar();
            if (ch < '0' || ch > '9') {
                source.rollbackPeek();
                return new Token(Type.ANY, ScopeType.NEUTRAL, pos, 1);
            }
        }

        if (ch == '0') {
            ch = source.peekChar();
        } else {
            do {
                ch = source.peekChar();
            } while (ch >= '0' && ch <= '9');
        }

        if (ch == '.') {
            int count = 0;
            do {
                ch = source.peekChar();
                count++;
            } while (ch >= '0' && ch <= '9');
            if (count == 1) {
                source.rollbackPeek();
                return new Token(Type.ANY, ScopeType.NEUTRAL, pos, 1);
            }
        }

        if (ch == 'e' || ch == 'E') {
            ch = source.peekChar();
            if (ch == '+' || ch == '-') {
                ch = source.peekChar();
            }
            int count;
            for (count = 0; ch >= '0' && ch <= '9'; count++) {
                ch = source.peekChar();
            }
            if (count == 0) {
                source.rollbackPeek();
                return new Token(Type.ANY, ScopeType.NEUTRAL, pos, 1);
            }
        }
        source.commitPeekBefore();
        return new Token(Type.NUMBER, ScopeType.NEUTRAL, pos, source.position() - pos);
    }


    /**
     * Read true.
     * @param source the lexer source
     * @return the token
     */
    private static Token readTrue(LexerSource source) {
        if (source.peekChar() == 'r' &&
            source.peekChar() == 'u' &&
            source.peekChar() == 'e') {
            source.commitPeek();
            return new Token(Type.LITERAL, ScopeType.NEUTRAL, source.position() - 4, 4);
        }
        source.rollbackPeek();
        return new Token(Type.ANY, ScopeType.NEUTRAL, source.position(), 1);
    }


    /**
     * Read false.
     * @param source the lexer source
     * @return the token
     */
    private static Token readFalse(LexerSource source) {
        if (source.peekChar() == 'a' &&
            source.peekChar() == 'l' &&
            source.peekChar() == 's' &&
            source.peekChar() == 'e') {
            source.commitPeek();
            return new Token(Type.LITERAL, ScopeType.NEUTRAL, source.position() - 5, 5);
        }
        source.rollbackPeek();
        return new Token(Type.ANY, ScopeType.NEUTRAL, source.position(), 1);
    }


    /**
     * Read null.
     * @param source the lexer source
     * @return the token
     */
    private static Token readNull(LexerSource source) {
        if (source.peekChar() == 'u' &&
            source.peekChar() == 'l' &&
            source.peekChar() == 'l') {
            source.commitPeek();
            return new Token(Type.LITERAL, ScopeType.NEUTRAL, source.position() - 4, 4);
        }
        source.rollbackPeek();
        return new Token(Type.ANY, ScopeType.NEUTRAL, source.position(), 1);
    }


    /**
     * Read line end.
     * @param source the lexer source
     * @return the token
     */
    private static Token lineEnd(LexerSource source) {
        char ch = source.peekChar();
        if (source.currentChar() == '\r' && ch == '\n' ||
            source.currentChar() == '\n' && ch == '\r') {
            source.commitPeek();
            return new Token(Type.EOL, ScopeType.INLINE_END, source.position(), 2);
        } else {
            return new Token(Type.EOL, ScopeType.INLINE_END, source.position(), 1);
        }
    }


    /**
     * Peek the next char.
     * @param source the lexer source
     * @return the next char
     */
    private char peekNextChar(LexerSource source) {
        char ch;
        for (;;) {
            ch = source.peekChar();
            if (ch == 0) break;
            if (!Character.isWhitespace(ch)) break;
        }
        source.rollbackPeek();
        return ch;
    }


    @Override
    public Coloring apply(int type) {
        return (type == Type.KEY) ? Coloring.DarkYellow :
               (type == Type.TEXT) ? Coloring.DarkGreen :
               (type == Type.NUMBER) ? Coloring.DarkSkyBlue :
               (type == Type.LITERAL) ? Coloring.DarkOrange :
               (type == Type.COMMENT) ? Coloring.DarkGray :
               (type == Type.LINE_COMMENT) ? Coloring.DarkGray : null;
    }

}
