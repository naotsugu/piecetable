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
package com.mammb.code.editor.syntax.type;

import com.mammb.code.editor.syntax.LexerSource;
import com.mammb.code.editor.syntax.ScopeType;
import com.mammb.code.editor.syntax.Token;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public interface TokenType {

    /** The number of serial. */
    AtomicInteger serial = new AtomicInteger(0);

    /** The type of any. */
    int ANY = serial.getAndIncrement();
    /** The type of empty. */
    int EMPTY = serial.getAndIncrement();
    /** The type of whitespace. */
    int SP = serial.getAndIncrement();
    /** The type of eol. */
    int EOL = serial.getAndIncrement();


    /**
     * Read line end.
     * @param source the lexer source
     * @return the token
     */
    static Token lineEnd(LexerSource source) {
        int pos = source.position();
        if (source.currentChar() == '\r' && source.peekChar() == '\n') {
            source.commitPeek();
            return new Token(TokenType.EOL, ScopeType.INLINE_END, pos, 2);
        } else {
            source.rollbackPeek();
            return new Token(TokenType.EOL, ScopeType.INLINE_END, pos, 1);
        }
    }


    /**
     * Get the empty token.
     * @param source the lexer source
     * @return the empty token
     */
    static Token empty(LexerSource source) {
        return Objects.isNull(source)
            ? new Token(TokenType.EMPTY, ScopeType.NEUTRAL, 0, 0)
            : new Token(TokenType.EMPTY, ScopeType.NEUTRAL, source.position(), 0);
    }


    /**
     * Get the whitespace token.
     * @param source the lexer source
     * @return the whitespace token
     */
    static Token whitespace(LexerSource source) {
        return new Token(TokenType.SP, ScopeType.NEUTRAL, source.position(), 1);
    }


    /**
     * Get an any token.
     * @param source the lexer source
     * @return an any token
     */
    static Token any(LexerSource source) {
        return new Token(TokenType.ANY, ScopeType.NEUTRAL, source.position(), 1);
    }

}

