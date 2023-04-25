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
import com.mammb.code.editor3.syntax.LexicalScope;
import com.mammb.code.editor3.syntax.ScopeType;
import com.mammb.code.editor3.syntax.Token;

import java.util.Optional;

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
        int INLINE_CODE = serial.getAndIncrement();
        int FENCE = serial.getAndIncrement();
    }

    /** The name. */
    private final String name;

    /** The input string. */
    private LexerSource source;

    /** The decorateTo. */
    private LexicalScope lexicalScope;

    /** The decorateTo. */
    private DecorateTo decorateTo = self;

    private Lexer delegate = null;


    /**
     * Constructor.
     * @param name the name
     * @param source the {@link LexerSource}
     */
    private MarkdownLexer(String name, LexerSource source) {
        this.name = name;
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @param source the {@link LexerSource}
     * @return a lexer
     */
    public static Lexer of(LexerSource source) {
        return new MarkdownLexer("", source);
    }


    /**
     * Create a new lexer.
     * @param name the name
     * @return a lexer
     */
    public static Lexer of(String name) {
        return new MarkdownLexer(name, null);
    }


    @Override
    public String name() {
        return name;
    }


    @Override
    public void setSource(LexerSource source, LexicalScope lexicalScope) {
        this.source = source;
        this.lexicalScope = lexicalScope;
    }


    @Override
    public Token nextToken() {

        if (source == null) {
            return TokenType.empty(null);
        }

        Optional<String> lexerName = lexicalScope.currentContext().stream()
            .findFirst().map(Token::note).map(Lexer::name);
        if (lexerName.isPresent()) {
            if (delegate == null || !delegate.name().equals(lexerName.get())) {
                delegate = Lexer.of(lexerName.get());
            }
            Token token = nextTokenDelegate();
            if (token != null) {
                return token;
            }
        }
        delegate = null;
        decorateTo = self;

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> TokenType.whitespace(source);
            case '\n', '\r' -> TokenType.lineEnd(source);
            case '#' -> readHeader(source);
            case '`' -> readFence(source);
            case 0 -> TokenType.empty(source);
            default -> TokenType.any(source);
        };
    }


    public Token nextTokenDelegate() {
        if (source.peekChar() == '`' && source.peekChar() == '`' && source.peekChar() == '`') {
            source.rollbackPeek();
            return null;
        }

        source.rollbackPeek();
        delegate.setSource(source, null);
        Token token = delegate.nextToken();
        this.decorateTo = (delegate instanceof DecorateTo c) ? c : DecorateTo.empty;
        return token;
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
            source.commitPeekBefore();
            return new Token(type, ScopeType.INLINE_ANY, pos, source.position() + 1 - pos);
        } else {
            source.rollbackPeek();
            return TokenType.any(source);
        }
    }


    /**
     * Read fence.
     * @param source the lexer source
     * @return the token
     */
    private Token readFence(LexerSource source) {
        int pos = source.position();
        if (source.peekChar() == '`' && source.peekChar() == '`') {
            source.commitPeek();
            StringBuilder sb = new StringBuilder();
            for (;;) {
                char ch = source.peekChar();
                if (Character.isLetterOrDigit(ch)) {
                    sb.append(ch);
                } else if (ch == '\n' || ch == 0) {
                    source.commitPeekBefore();
                    return new Token(Type.FENCE, ScopeType.CONTEXT_START, pos, source.position() + 1 - pos, sb.toString());
                } else {
                    source.rollbackPeek();
                    return new Token(Type.FENCE, ScopeType.CONTEXT_ANY, pos, source.position() + 1 - pos);
                }
            }
        } else {
            source.rollbackPeek();
            return new Token(Type.INLINE_CODE, ScopeType.INLINE_ANY, pos, 1);
        }
    }


    @Override
    public Decorated apply(int type) {
        return decorateTo.apply(type);
    }


    /** The markdown decorateTo. */
    public static final DecorateTo self = type ->
        (type == Type.H1) ? Decorated.of(20, Coloring.DarkSkyBlue) :
        (type == Type.H2) ? Decorated.of(19, Coloring.DarkSkyBlue) :
        (type == Type.H3) ? Decorated.of(18, Coloring.DarkSkyBlue) :
        (type == Type.H4) ? Decorated.of(17, Coloring.DarkSkyBlue) :
        (type == Type.H5) ? Decorated.of(16, Coloring.DarkSkyBlue) :
        (type == Type.INLINE_CODE) ? Decorated.of(Coloring.DarkBrown) : Decorated.empty();
}
