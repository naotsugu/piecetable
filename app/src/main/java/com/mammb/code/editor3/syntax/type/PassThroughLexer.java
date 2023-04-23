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
 * PassThrough Lexer.
 * @author Naotsugu Kobayashi
 */
public class PassThroughLexer implements Lexer, DecorateTo {

    /** The input string. */
    private LexerSource source;

    /** The flag of finished. */
    private boolean finished = false;


    /**
     * Constructor.
     * @param source the {@link LexerSource}
     */
    private PassThroughLexer(LexerSource source) {
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @param source the {@link LexerSource}
     * @return a lexer
     */
    public static Lexer of(LexerSource source) {
        return new PassThroughLexer(source);
    }


    /**
     * Create a new lexer.
     * @return a lexer
     */
    public static Lexer of() {
        return new PassThroughLexer(null);
    }


    @Override
    public void setSource(LexerSource source) {
        this.finished = false;
        this.source = source;
    }


    @Override
    public Token nextToken() {
        if (source == null || finished) {
            return new Token(0, ScopeType.NEUTRAL, 0, 0);
        }
        finished = true;
        return new Token(0, ScopeType.NEUTRAL, 0, source.length());
    }


    @Override
    public Decorated apply(int type) {
        return Decorated.empty();
    }

}
