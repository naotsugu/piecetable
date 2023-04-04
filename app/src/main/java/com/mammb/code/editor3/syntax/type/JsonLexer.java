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
public class JsonLexer implements Lexer {

    /** Token type. */
    public enum TokenType {ANY, EMPTY, SP, EOL, TEXT, NUMBER, LITERAL;}

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

        if (source == null) return new Token(TokenType.EMPTY.ordinal(), ScopeType.NEUTRAL, 0, 0);

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> new Token(TokenType.SP.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
            case '\n', '\r' -> lineEnd(source);
            case '"' -> readString(source);
            case 't' -> readTrue(source);
            case 'f' -> readFalse(source);
            case 'n' -> readNull(source);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' -> readNumber(source);
            case 0 -> new Token(TokenType.EMPTY.ordinal(), ScopeType.NEUTRAL, 0, 0);
            default -> new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
        };
    }


    private Token readString(LexerSource source) {
        int pos = source.position();
        char prev = 0;
        for (; ; ) {
            char ch = source.peekChar();
            if (ch < ' ' || (prev != '\\' && ch == '"')) {
                source.commitPeek();
                return new Token(TokenType.TEXT.ordinal(), ScopeType.NEUTRAL, pos, source.position() + 1 - pos);
            }
            prev = ch;
        }
    }


    private Token readNumber(LexerSource source) {

        int pos = source.position();
        char ch = source.currentChar();

        if (ch == '-') {
            ch = source.peekChar();
            if (ch < '0' || ch > '9') {
                source.rollbackPeek();
                return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, pos, 1);
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
                return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, pos, 1);
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
                return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, pos, 1);
            }
        }
        source.commitPeekBefore();
        return new Token(TokenType.NUMBER.ordinal(), ScopeType.NEUTRAL, pos, source.position() - pos);
    }


    private static Token readTrue(LexerSource source) {
        if (source.peekChar() == 'r' &&
            source.peekChar() == 'u' &&
            source.peekChar() == 'e') {
            source.commitPeek();
            return new Token(TokenType.LITERAL.ordinal(), ScopeType.NEUTRAL, source.position() - 4, 4);
        }
        source.rollbackPeek();
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }


    private static Token readFalse(LexerSource source) {
        if (source.peekChar() == 'a' &&
            source.peekChar() == 'l' &&
            source.peekChar() == 's' &&
            source.peekChar() == 'e') {
            source.commitPeek();
            return new Token(TokenType.LITERAL.ordinal(), ScopeType.NEUTRAL, source.position() - 5, 5);
        }
        source.rollbackPeek();
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }


    private static Token readNull(LexerSource source) {
        if (source.peekChar() == 'u' &&
            source.peekChar() == 'l' &&
            source.peekChar() == 'l') {
            source.commitPeek();
            return new Token(TokenType.LITERAL.ordinal(), ScopeType.NEUTRAL, source.position() - 4, 4);
        }
        source.rollbackPeek();
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }


    private static Token lineEnd(LexerSource source) {
        char ch = source.peekChar();
        if (source.currentChar() == '\r' && ch == '\n' ||
            source.currentChar() == '\n' && ch == '\r') {
            source.commitPeek();
            return new Token(TokenType.EOL.ordinal(), ScopeType.INLINE_END, source.position(), 2);
        } else {
            return new Token(TokenType.EOL.ordinal(), ScopeType.INLINE_END, source.position(), 1);
        }
    }


    @Override
    public ColoringTo coloringTo() {
        return type ->
            (type == TokenType.TEXT.ordinal()) ? Coloring.DarkGreen :
            (type == TokenType.NUMBER.ordinal()) ? Coloring.DarkSkyBlue :
            (type == TokenType.LITERAL.ordinal()) ? Coloring.DarkOrange : null;
    }

}
