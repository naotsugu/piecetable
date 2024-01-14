/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.editor.syntax;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Text for {@link LexerSourceUtf16}.
 * @author Naotsugu Kobayashi
 */
class LexerSourceUtf16Test {

    @Test
    void testReadChar() {
        var source = LexerSourceUtf16.of(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_16)));
        assertEquals('a', source.readChar());
        assertEquals('b', source.readChar());
        assertEquals('c', source.readChar());
        assertEquals(0, source.readChar());
    }

    @Test
    void testReadCharWide() {
        var source = LexerSourceUtf16.of(new ByteArrayInputStream("あい".getBytes(StandardCharsets.UTF_16)));
        assertEquals('あ', source.readChar());
        assertEquals('い', source.readChar());
        assertEquals(0, source.readChar());
    }

    @Test
    void testReadCharSurrogate() {
        var source = LexerSourceUtf16.of(new ByteArrayInputStream("😊".getBytes(StandardCharsets.UTF_16)));
        char h = source.readChar();
        char l = source.readChar();
        assertEquals("😊", new String(new char[] { h, l }));
        assertEquals(0, source.readChar());
    }

    @Test
    void testPeekChar() {
        var source = LexerSourceUtf16.of(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_16)));

        assertEquals('a', source.peekChar());
        assertEquals('b', source.peekChar());
        assertEquals('c', source.peekChar());
        assertEquals(0, source.peekChar());

        assertEquals('a', source.readChar());
        assertEquals('b', source.peekChar());
        source.commitPeek();
        assertEquals('c', source.readChar());
    }

}
