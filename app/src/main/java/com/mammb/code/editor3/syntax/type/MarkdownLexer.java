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
import com.mammb.code.editor3.model.Decorated;
import com.mammb.code.editor3.syntax.DecorateTo;
import com.mammb.code.editor3.syntax.Lexer;
import com.mammb.code.editor3.syntax.LexerSource;
import com.mammb.code.editor3.syntax.ScopeType;
import com.mammb.code.editor3.syntax.Token;

/**
 * MarkdownLexer.
 * @author Naotsugu Kobayashi
 */
public class MarkdownLexer implements Lexer, DecorateTo {

    /** Token type. */
    protected interface Type extends TokenType {
        int H1 = serial.getAndIncrement();
        int H2 = serial.getAndIncrement();
        int H3 = serial.getAndIncrement();
        int H4 = serial.getAndIncrement();
        int H5 = serial.getAndIncrement();
    }

    /** The input string. */
    private LexerSource source;


    /**
     * Constructor.
     * @param source the {@link LexerSource}
     */
    private MarkdownLexer(LexerSource source) {
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @param source the {@link LexerSource}
     * @return a lexer
     */
    public static Lexer of(LexerSource source) {
        return new MarkdownLexer(source);
    }


    /**
     * Create a new lexer.
     * @return a lexer
     */
    public static Lexer of() {
        return new MarkdownLexer(null);
    }


    @Override
    public void setSource(LexerSource source) {
        this.source = source;
    }


    @Override
    public Token nextToken() {

        if (source == null) return TokenType.empty(null);

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> TokenType.whitespace(source);
            case '\n', '\r' -> TokenType.lineEnd(source);
            case '#' -> readHeader(source);
            case 0 -> TokenType.empty(source);
            default -> TokenType.any(source);
        };
    }


    /**
     * Read header.
     * @param source the lexer source
     * @return the token
     */
    private Token readHeader(LexerSource source) {

        int pos = source.position();
        char[] ca = new char[] { source.peekChar(), source.peekChar(),
            source.peekChar(), source.peekChar(), source.peekChar()};

        int type = 0;
        if (ca[0] == ' ') type = Type.H1;
        else if (ca[0] == '#' && ca[1] == ' ') type = Type.H2;
        else if (ca[0] == '#' && ca[1] == '#' && ca[2] == ' ') type = Type.H3;
        else if (ca[0] == '#' && ca[1] == '#' && ca[2] == '#' && ca[3] == ' ') type = Type.H4;
        else if (ca[0] == '#' && ca[1] == '#' && ca[2] == '#' && ca[3] == '#' && ca[4] == ' ') type = Type.H5;

        if (type > 0) {
            for (;;) { if (source.peekChar() == '\n') break; }
            source.commitPeek();
            return new Token(type, ScopeType.INLINE_ANY, pos, source.position() + 1 - pos);
        } else {
            source.rollbackPeek();
            return TokenType.any(source);
        }
    }


    @Override
    public Decorated apply(int type) {
        return (type == Type.H1) ? Decorated.of(20, Coloring.DarkSkyBlue) :
               (type == Type.H2) ? Decorated.of(19, Coloring.DarkSkyBlue) :
               (type == Type.H3) ? Decorated.of(18, Coloring.DarkSkyBlue) :
               (type == Type.H4) ? Decorated.of(17, Coloring.DarkSkyBlue) :
               (type == Type.H5) ? Decorated.of(16, Coloring.DarkSkyBlue) : Decorated.empty();
    }

}
