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

import com.mammb.code.editor3.syntax.Lexer;
import com.mammb.code.editor3.syntax.LexerSource;
import com.mammb.code.editor3.syntax.ScopeType;
import com.mammb.code.editor3.syntax.Token;
import com.mammb.code.editor3.syntax.TokenType;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public class JsonLexer implements Lexer {

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

        if (source == null) return Lexer.empty();

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> Lexer.whitespace(source);
            case '\n', '\r' -> Lexer.lineEnd(source);
            case '"' -> readString(source);
            case 't' -> readTrue(source);
            case 'f' -> readFalse(source);
            case 'n' -> readNull(source);
            case 0 -> Lexer.empty(source);
            default -> new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
        };
    }


    private Token readString(LexerSource source) {
        int pos = source.position();
        char prev = 0;
        for (;;) {
            char ch = source.peekChar();
            if (ch < ' ' || (prev != '\\' && ch == '"')) {
                source.commitPeek();
                return new Token(TokenType.TEXT.ordinal(), ScopeType.NEUTRAL, pos, source.position() - pos);
            }
            prev = ch;
        }
    }


    private Token readNumber(LexerSource source) {
        // TODO
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }


    public static Token readTrue(LexerSource source) {
        if (source.peekChar() == 'r' &&
            source.peekChar() == 'u' &&
            source.peekChar() == 'e') {
            source.commitPeek();
            return new Token(TokenType.LITERAL.ordinal(), ScopeType.NEUTRAL, source.position() - 4, 4);
        }
        source.rollbackPeek();
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }


    public static Token readFalse(LexerSource source) {
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


    public static Token readNull(LexerSource source) {
        if (source.peekChar() == 'u' &&
            source.peekChar() == 'l' &&
            source.peekChar() == 'l') {
            source.commitPeek();
            return new Token(TokenType.LITERAL.ordinal(), ScopeType.NEUTRAL, source.position() - 4, 4);
        }
        source.rollbackPeek();
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }

}
