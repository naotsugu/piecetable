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
import com.mammb.code.editor3.syntax.Token;
import com.mammb.code.editor3.syntax.TokenType;
import com.mammb.code.editor3.syntax.Trie;
import java.util.stream.Stream;

/**
 * JavaLexer.
 * @author Naotsugu Kobayashi
 */
public class JavaLexer implements Lexer {

    /** The syntax keywords. */
    private static final Trie keywords = keywords();

    /** The input string. */
    private LexerSource source;


    /**
     * Constructor.
     * @param source the {@link LexerSource}
     */
    private JavaLexer(LexerSource source) {
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @param source the {@link LexerSource}
     * @return a lexer
     */
    public static Lexer of(LexerSource source) {
        return new JavaLexer(source);
    }


    @Override
    public Token nextToken() {
        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t', '\n', '\r' -> whitespace(source);
            case 0 -> empty(source);
            default -> Character.isJavaIdentifierStart(ch)
                ? readIdentifier(source)
                : any(source);
        };
    }


    private Token readIdentifier(LexerSource source) {

        int pos = source.position();
        StringBuilder sb = new StringBuilder();
        sb.append(source.currentChar());

        for (;;) {
            char ch = source.peekChar();
            if (!Character.isJavaIdentifierPart(ch)) {
                source.rollbackPeek();
                String str = sb.toString();
                TokenType type = keywords.match(str) ? TokenType.KEYWORD : TokenType.ANY;
                return new Token(type.ordinal(), pos, str.length());
            }
            sb.append(source.readChar());
        }
    }


    private static Token any(LexerSource source) {
        return new Token(TokenType.ANY.ordinal(), source.position(), 1);
    }

    private static Token empty(LexerSource source) {
        return new Token(TokenType.EMPTY.ordinal(), source.position(), 0);
    }

    private static Token whitespace(LexerSource source) {
        return new Token(TokenType.SP.ordinal(), source.position(), 1);
    }


    private static Trie keywords() {
        Trie trie = new Trie();
        Stream.of("""
        abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,
        this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,
        return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,
        strictfp,volatile,const,float,native,super,while,var,record,sealed,with,yield,to,transitive,uses"""
            .split("[,\\s]")).forEach(trie::put);
        return trie;
    }

}
