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

import com.mammb.code.editor3.lang.Numbers;
import com.mammb.code.editor3.model.Coloring;
import com.mammb.code.editor3.model.Decorated;
import com.mammb.code.editor3.syntax.DecorateTo;
import com.mammb.code.editor3.syntax.Lexer;
import com.mammb.code.editor3.syntax.LexerSource;
import com.mammb.code.editor3.syntax.LexicalScope;
import com.mammb.code.editor3.syntax.ScopeType;
import com.mammb.code.editor3.syntax.Token;
import com.mammb.code.editor3.syntax.Trie;
import java.util.stream.Stream;

/**
 * JavaLexer.
 * @author Naotsugu Kobayashi
 */
public class JavaLexer implements Lexer, DecorateTo {

    /** Token type. */
    protected interface Type extends TokenType {
        int KEYWORD = serial.getAndIncrement();
        int TEXT = serial.getAndIncrement();
        int NUMBER = serial.getAndIncrement();
        int LINE_COMMENT = serial.getAndIncrement();
        int COMMENT = serial.getAndIncrement();
        int CHAR_LITERAL = serial.getAndIncrement();
    }

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
    public void setSource(LexerSource source, LexicalScope lexicalScope) {
        this.source = source;
    }


    @Override
    public Token nextToken() {

        if (source == null) return TokenType.empty(null);

        char ch = source.readChar();
        return switch (ch) {
            case ' ', '\t' -> TokenType.whitespace(source);
            case '\n', '\r' -> TokenType.lineEnd(source);
            case '/' -> readComment(source);
            case '*'  -> readCommentBlockClosed(source);
            case '"'  -> readText(source);
            case '\'' -> readChar(source);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-' -> readNumber(source);
            case 0 -> TokenType.empty(source);
            default -> Character.isJavaIdentifierStart(ch)
                ? readIdentifier(source)
                : TokenType.any(source);
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
            return new Token(Type.LINE_COMMENT, ScopeType.INLINE_START, pos, 2);
        } else if (ch == '*') {
            source.commitPeek();
            return new Token(Type.COMMENT, ScopeType.BLOCK_START, pos, 2);
        } else {
            return TokenType.any(source);
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
            return new Token(Type.COMMENT, ScopeType.BLOCK_END, pos, 2);
        } else {
            return TokenType.any(source);
        }
    }


    /**
     * Read text.
     * @param source the lexer source
     * @return the token
     */
    private Token readText(LexerSource source) {
        if (source.peekChar() == '"' && source.peekChar() == '"') {
            source.commitPeek();
            return new Token(Type.TEXT, ScopeType.BLOCK_ANY, source.position() - 3, 3);
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
            if (ch < ' ') {
                source.rollbackPeek();
                return TokenType.any(source);
            } else if (prev != '\\' && ch == '"') {
                source.commitPeek();
                return new Token(Type.TEXT, ScopeType.NEUTRAL, pos, source.position() + 1 - pos);
            }
            prev = ch;
        }
    }


    /**
     * Read char.
     * @param source the lexer source
     * @return the token
     */
    private Token readChar(LexerSource source) {
        int pos = source.position();
        char prev = 0;
        for (;;) {
            char ch = source.peekChar();
            if (ch < ' ') {
                source.rollbackPeek();
                return TokenType.any(source);
            } else if (prev != '\\' && ch == '\'') {
                source.commitPeek();
                return new Token(Type.CHAR_LITERAL, ScopeType.NEUTRAL, pos, source.position() + 1 - pos);
            }
            prev = ch;
        }
    }


    /**
     * Read the number.
     * @param source the lexer source
     * @return the token
     */
    private Token readNumber(LexerSource source) {
        StringBuilder sb = new StringBuilder();
        sb.append(source.currentChar());
        int pos = source.position();
        for (;;) {
            char ch = source.peekChar();
            if (Numbers.isJavaNumberPart(ch)) {
                sb.append(ch);
            } else {
                break;
            }
        }
        if (Numbers.isJavaNumber(sb.toString())) {
            source.commitPeekBefore();
            return new Token(Type.NUMBER, ScopeType.NEUTRAL, pos, sb.length());
        } else {
            return TokenType.any(source);
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
            if (!isIdentifierPart(ch)) {
                String str = sb.toString();
                source.commitPeekBefore();
                if (keywords.match(str)) {
                    return new Token(Type.KEYWORD, ScopeType.NEUTRAL, pos, str.length());
                } else {
                    return new Token(Type.ANY, ScopeType.NEUTRAL, pos, str.length());
                }
            }
            sb.append(source.readChar());
        }

    }


    /**
     * Determines if the specified character may be part of a Java identifier
     * as other than the first character.
     * @param ch the character to be tested
     * @return {@code true}, if the character may be part of a Java identifier
     */
    private boolean isIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
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


    @Override
    public Decorated apply(int type) {
        return (type == Type.NUMBER) ? Decorated.of(Coloring.DarkSkyBlue) :
               (type == Type.COMMENT) ? Decorated.of(Coloring.DarkGreen) :
               (type == Type.LINE_COMMENT) ? Decorated.of(Coloring.DarkGray) :
               (type == Type.KEYWORD) ? Decorated.of(Coloring.DarkOrange) :
               (type == Type.TEXT) ? Decorated.of(Coloring.DarkGreen) : Decorated.empty();
    }

}
