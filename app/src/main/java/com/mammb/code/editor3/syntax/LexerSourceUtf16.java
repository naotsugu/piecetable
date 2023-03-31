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
package com.mammb.code.editor3.syntax;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * LexerSourceUtf16.
 * @author Naotsugu Kobayashi
 */
public class LexerSourceUtf16 implements LexerSource {

    /** The input string. */
    private InputStream input;

    /** The current position in input (points to current char). */
    private int position = -1;

    /** The peeked count. */
    private int peekCount = 0;

    /** The current char. */
    private char currentChar = 0;

    /** little endian?. */
    private Boolean littleEndian = null;

    /** length(cached). */
    private int length = -1;


    private LexerSourceUtf16(InputStream input) {
        this.input = input.markSupported() ? input : new BufferedInputStream(input);
        try {input.readAllBytes();
            input.mark(2);
            char high = (char) input.read();
            char low  = (char) input.read();
            input.reset();
            if (high == 0xFE && low == 0xFF) {
                // big-endian
                input.skipNBytes(2);
                littleEndian = false;
            } else if (high == 0xFF && low == 0xFE) {
                // little-endian
                input.skipNBytes(2);
                littleEndian = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static LexerSource of(InputStream input) {
        return new LexerSourceUtf16(input);
    }


    @Override
    public char readChar() {
        int read = read();
        currentChar = (read == -1) ? 0 : (char) read;
        return currentChar;
    }


    @Override
    public char currentChar() {
        return currentChar;
    }


    @Override
    public char peekChar() {
        int peek = peek();
        return (peek == -1) ? 0 : (char) peek;
    }


    @Override
    public void commitPeek() {
        try {
            if (peekCount > 0) {
                input.reset();
                input.skipNBytes(peekCount * 2L);
                peekCount = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void rollbackPeek() {
        try {
            input.reset();
            peekCount = 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int position() {
        return position;
    }


    @Override
    public int length() {
        if (length < 0) {
            if (peekCount > 0) {
                throw new IllegalStateException("peeking in process");
            }
            try {
                input.mark(Integer.MAX_VALUE);
                length = input.readAllBytes().length / 2;
                input.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (Objects.nonNull(littleEndian)) {
                length -= 2;
            }
        }
        return length;
    }


    private int read() {
        try {
            if (peekCount > 0) {
                input.reset();
                peekCount = 0;
            }
            position++;
            byte[] bytes = new byte[2];
            int ret = input.read(bytes);
            if (ret < 2) {
                return -1;
            }
            return bytesToChar(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private int peek() {
        try {
            if (peekCount == 0) {
                input.mark(Integer.MAX_VALUE);
            }
            peekCount++;
            byte[] bytes = new byte[2];
            int ret = input.read(bytes);
            if (ret < 2) {
                return -1;
            }
            return bytesToChar(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public char bytesToChar(byte[] b) {
        if (b.length != 2) {
            return 0;
        }
        if (Objects.nonNull(littleEndian) && littleEndian) {
            return (char) ((b[1] << 8) & 0xFF00 | (b[0] & 0x00FF));
        } else {
            return (char) ((b[0] << 8) & 0xFF00 | (b[1] & 0x00FF));
        }
    }

}
