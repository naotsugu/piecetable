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

import com.mammb.code.editor.model.Decorating;
import com.mammb.code.editor.syntax.DecorateTo;
import com.mammb.code.editor.syntax.Lexer;
import com.mammb.code.editor.syntax.LexerSource;
import com.mammb.code.editor.syntax.LexicalScope;
import com.mammb.code.editor.syntax.ScopeType;
import com.mammb.code.editor.syntax.Token;

/**
 * PassThrough Lexer.
 * @author Naotsugu Kobayashi
 */
public class PassThroughLexer implements Lexer, DecorateTo {

    /** The name. */
    private final String name;

    /** The input string. */
    private LexerSource source;


    /**
     * Constructor.
     * @param name the name
     * @param source the {@link LexerSource}
     */
    private PassThroughLexer(String name, LexerSource source) {
        this.name = name;
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @param source the {@link LexerSource}
     * @return a lexer
     */
    public static Lexer of(LexerSource source) {
        return new PassThroughLexer("", source);
    }


    /**
     * Create a new lexer.
     * @param name the name
     * @return a lexer
     */
    public static Lexer of(String name) {
        return new PassThroughLexer(name, null);
    }


    @Override
    public String name() {
        return name;
    }


    @Override
    public void setSource(LexerSource source, LexicalScope lexicalScope) {
        this.source = source;
    }


    @Override
    public Token nextToken() {

        if (source == null) {
            return TokenType.empty(null);
        }

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> TokenType.whitespace(source);
            case '\n', '\r' -> TokenType.lineEnd(source);
            case 0 -> new Token(TokenType.EMPTY, ScopeType.NEUTRAL, 0, 0);
            default -> TokenType.any(source);
        };
    }


    @Override
    public Decorating apply(int type) {
        return Decorating.empty();
    }

}
