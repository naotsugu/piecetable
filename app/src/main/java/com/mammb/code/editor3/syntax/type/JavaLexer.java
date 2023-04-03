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


    /**
     * Create a new lexer.
     * @return a lexer
     */
    public static Lexer of() {
        return new JavaLexer(null);
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
            case '/' -> readComment(source);
            case '*'  -> readCommentBlockClosed(source);
            case '"'  -> readText(source);
            case 0 -> Lexer.empty(source);
            default -> Character.isJavaIdentifierStart(ch)
                ? readIdentifier(source)
                : Lexer.any(source);
        };
    }


    /**
     * Read comment.
     * @param source the lexer source
     * @return the token
     */
    private Token readComment(LexerSource source) {
        int pos = source.position();
        char ch = source.peekChar();
        if (ch == '/') {
            source.commitPeek();
            return new Token(TokenType.LINE_COMMENT.ordinal(), ScopeType.INLINE_START, pos, 2);
        } else if (ch == '*') {
            source.commitPeek();
            return new Token(TokenType.COMMENT.ordinal(), ScopeType.BLOCK_START, pos, 2);
        } else {
            return Lexer.any(source);
        }
    }


    /**
     * Read block comment.
     * @param source the lexer source
     * @return the token
     */
    private Token readCommentBlockClosed(LexerSource source) {
        int pos = source.position();
        char ch = source.peekChar();
        if (ch == '/') {
            source.commitPeek();
            return new Token(TokenType.COMMENT.ordinal(), ScopeType.BLOCK_END, pos, 2);
        } else {
            return Lexer.any(source);
        }
    }


    /**
     * Read text.
     * @param source the lexer source
     * @return the token
     */
    private Token readText(LexerSource source) {
        if (source.peekChar() == '"' &&
            source.peekChar() == '"') {
            source.commitPeek();
            return new Token(TokenType.TEXT.ordinal(), ScopeType.BLOCK_ANY, source.position() - 3, 3);
        }
        source.rollbackPeek();
        return readString(source);
    }


    /**
     * Read string.
     * @param source the lexer source
     * @return the token
     */
    private Token readString(LexerSource source) {
        int pos = source.position();
        char prev = 0;
        for (;;) {
            char ch = source.peekChar();
            if (ch < ' ' || (prev != '\\' && ch == '"')) {
                source.commitPeek();
                return new Token(TokenType.TEXT.ordinal(), ScopeType.NEUTRAL, pos, source.position() + 1 - pos);
            }
            prev = ch;
        }
    }


    /**
     * Read identifier.
     * @param source the lexer source
     * @return the token
     */
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
                return new Token(type.ordinal(), ScopeType.NEUTRAL, pos, str.length());
            }
            sb.append(source.readChar());
        }
    }


    /**
     * Get the keyword trie.
     * @return the keyword trie
     */
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
