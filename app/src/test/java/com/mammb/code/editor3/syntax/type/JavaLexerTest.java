package com.mammb.code.editor3.syntax.type;

import com.mammb.code.editor3.syntax.LexerSource;
import com.mammb.code.editor3.syntax.Token;
import com.mammb.code.editor3.syntax.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaLexerTest {

    @Test
    void nextToken() {

        var lexer = JavaLexer.of(LexerSource.of("public static void main(String... args) {"));
        assertEquals(new Token(TokenType.KEYWORD.ordinal(), 0, 6), lexer.nextToken());
        assertEquals(new Token(TokenType.SP.ordinal(), 6, 1), lexer.nextToken());
        assertEquals(new Token(TokenType.KEYWORD.ordinal(), 7, 6), lexer.nextToken());
        assertEquals(new Token(TokenType.SP.ordinal(), 13, 1), lexer.nextToken());
        assertEquals(new Token(TokenType.KEYWORD.ordinal(), 14, 4), lexer.nextToken());
        assertEquals(new Token(TokenType.SP.ordinal(), 18, 1), lexer.nextToken());
        assertEquals(new Token(TokenType.ANY.ordinal(), 19, 4), lexer.nextToken());
    }
}
