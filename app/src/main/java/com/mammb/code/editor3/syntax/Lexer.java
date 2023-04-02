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
package com.mammb.code.editor3.syntax;

import com.mammb.code.editor3.syntax.type.JavaLexer;
import com.mammb.code.editor3.syntax.type.JsonLexer;
import com.mammb.code.editor3.syntax.type.PassThroughLexer;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public interface Lexer {

    /**
     * Gets the next token.
     * @return the next token
     */
    Token nextToken();


    /**
     * Set the source.
     * @param source the source.
     */
    void setSource(LexerSource source);


    static Lexer of(String ext) {
        return switch (ext) {
            case "java" -> JavaLexer.of();
            case "json" -> JsonLexer.of();
            default -> PassThroughLexer.of();
        };
    }

    static Token any(LexerSource source) {
        return new Token(TokenType.ANY.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }

    static Token empty(LexerSource source) {
        return new Token(TokenType.EMPTY.ordinal(), ScopeType.NEUTRAL, source.position(), 0);
    }

    static Token empty() {
        return new Token(TokenType.EMPTY.ordinal(), ScopeType.NEUTRAL, 0, 0);
    }

    static Token whitespace(LexerSource source) {
        return new Token(TokenType.SP.ordinal(), ScopeType.NEUTRAL, source.position(), 1);
    }

    static Token lineEnd(LexerSource source) {
        char ch = source.peekChar();
        if (source.currentChar() == '\r' && ch == '\n' ||
            source.currentChar() == '\n' && ch == '\r') {
            source.commitPeek();
            return new Token(TokenType.EOL.ordinal(), ScopeType.INLINE_END, source.position(), 2);
        } else {
            return new Token(TokenType.EOL.ordinal(), ScopeType.INLINE_END, source.position(), 1);
        }
    }

}
