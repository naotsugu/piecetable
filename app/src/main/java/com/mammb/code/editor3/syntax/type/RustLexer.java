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
import com.mammb.code.editor3.syntax.Trie;
import java.util.stream.Stream;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public class RustLexer implements Lexer, DecorateTo {

    /** Token type. */
    protected interface Type extends TokenType {
        int KEYWORD = serial.getAndIncrement();
        int TEXT = serial.getAndIncrement();
        int NUMBER = serial.getAndIncrement();
        int LINE_COMMENT = serial.getAndIncrement();
        int COMMENT = serial.getAndIncrement();
        int DOC_COMMENT = serial.getAndIncrement();
    }

    /** The syntax keywords. */
    private static final Trie keywords = keywords();

    /** The input string. */
    private LexerSource source;


    /**
     * Constructor.
     * @param source the {@link LexerSource}
     */
    private RustLexer(LexerSource source) {
        this.source = source;
    }


    /**
     * Create a new lexer.
     * @return a lexer
     */
    public static Lexer of() {
        return new RustLexer(null);
    }


    @Override
    public void setSource(LexerSource source) {
        this.source = source;
    }


    @Override
    public Token nextToken() {

        if (source == null) return TokenType.empty(null);

        int ch = source.readChar();
        if (Character.isHighSurrogate((char) ch)) {
            ch = Character.toCodePoint((char) ch, source.currentChar());
        }
        return switch (ch) {
            case ' ', '\t' -> TokenType.whitespace(source);
            case '\n', '\r' -> TokenType.lineEnd(source);
            case '/' -> readComment(source);
            case '*'  -> readCommentBlockClosed(source);
            case '"'  -> readString(source);
            case 0 -> TokenType.empty(source);
            default -> isIdentifierStart(ch)
                ? readIdentifier(source, ch)
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
            char nch = source.peekChar();
            if (nch == '/' || nch == '!') {
                source.commitPeek();
                return new Token(Type.DOC_COMMENT, ScopeType.INLINE_START, pos, 3);
            } else {
                source.commitPeek();
                return new Token(Type.LINE_COMMENT, ScopeType.INLINE_START, pos, 2);
            }
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
     * Read identifier.
     * @param source the lexer source
     * @return the token
     */
    private Token readIdentifier(LexerSource source, int cp) {

        int pos = source.position();
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(cp));

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
     * Determines if the character (Unicode code point) is permissible as the first character in an identifier.
     * @param cp the character (Unicode code point) to be tested
     * @return {@code true}, if the character may start an identifier
     */
    private boolean isIdentifierStart(int cp) {
        int type = Character.getType(cp);
        return (type == Character.UPPERCASE_LETTER || type == Character.LOWERCASE_LETTER ||
                type == Character.TITLECASE_LETTER || type == Character.MODIFIER_LETTER ||
                type == Character.OTHER_LETTER ||  type == Character.LETTER_NUMBER);
    }


    /**
     * Determines if the specified character may be part of a rust identifier
     * as other than the first character.
     * @param cp the character to be tested
     * @return {@code true}, if the character may be part of a rust identifier
     */
    private boolean isIdentifierPart(int cp) {
        int type = Character.getType(cp);
        return (type == Character.UPPERCASE_LETTER || type == Character.LOWERCASE_LETTER ||
                type == Character.TITLECASE_LETTER || type == Character.MODIFIER_LETTER ||
                type == Character.OTHER_LETTER ||  type == Character.LETTER_NUMBER ||
                type == Character.NON_SPACING_MARK ||  type == Character.COMBINING_SPACING_MARK ||
                type == Character.DECIMAL_DIGIT_NUMBER || type == Character.CONNECTOR_PUNCTUATION);
    }


    /**
     * Get the keyword trie.
     * @return the keyword trie
     */
    private static Trie keywords() {
        Trie trie = new Trie();
        Stream.of("""
            as, break, const, continue, crate, else, enum, extern, false, fn, for, if, impl, in,
            let, loop, match, mod, move, mut, pub, ref, return, self, Self, static, struct, super,
            trait, true, type, unsafe, use, where, while, async, await, dyn, try
            abstract, become, box, do, final, macro, override, priv, typeof, unsized, virtual,yield"""
            .split("[,\\s]")).forEach(trie::put);
        return trie;
    }


    @Override
    public Decorated apply(int type) {
        return (type == Type.NUMBER) ? Decorated.of(Coloring.DarkSkyBlue) :
               (type == Type.COMMENT) ? Decorated.of(Coloring.DarkGray) :
               (type == Type.LINE_COMMENT) ? Decorated.of(Coloring.DarkGray) :
               (type == Type.DOC_COMMENT) ? Decorated.of(Coloring.DarkGreen) :
               (type == Type.KEYWORD) ? Decorated.of(Coloring.DarkOrange) :
               (type == Type.TEXT) ? Decorated.of(Coloring.DarkGreen) : Decorated.empty();
    }

}
