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

        if (source == null) {
            return new Token(TokenType.EMPTY.ordinal(), ScopeType.NEUTRAL, 0, 0);
        }

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t', '\n', '\r' -> whitespace(source);
            case 0 -> empty(source);
            default -> Character.isJavaIdentifierStart(ch)
                ? readIdentifier(source)
                : any(source);
        };
    }


    private static Token any(LexerSource source) {
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }

    private static Token empty(LexerSource source) {
        return new Token(TokenType.EMPTY.ordinal(), ScopeType.NEUTRAL, source.position(), 0);
    }

    private static Token whitespace(LexerSource source) {
        return new Token(TokenType.SP.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }



    private Token readIdentifier(LexerSource source) {
        return null;
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
