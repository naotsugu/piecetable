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
package com.mammb.code.piecetable.buffer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The utility of UTF-8.
 * @author Naotsugu Kobayashi
 */
public abstract class Utf8 {

    /**
     * Private constructor.
     */
    private Utf8() { }


    /**
     * Get whether the given byte is a utf-8 lower.
     * @param b the byte to be checked
     * @return {@code true}, if given byte is a utf-8 lower
     */
    public static boolean isLower(byte b) {
        // 10.. ....
        return (b & 0xC0) == 0x80;
    }


    /**
     * Get the number of bytes of utf-8 code following a given byte.
     * @param b the byte to be checked
     * @return the number of bytes of utf-8 code following a given byte
     */
    public static short followsCount(byte b) {
        if ((b & 0x80) == 0x00) {
            // 0... ....
            return 1;
        } else if ((b & 0xE0) == 0xC0) {
            // 110. ....
            return 2;
        } else if ((b & 0xF0) == 0xE0) {
            // 1110 ....
            return 3;
        } else if ((b & 0xF8) == 0xF0) {
            // 1111 0...
            return 4;
        } else {
            throw new IllegalArgumentException(Byte.toString(b));
        }
    }


    /**
     * Gets a utf-8 byte array from the given byte array.
     * @param bytes the byte array
     * @param index the index of byte array
     * @return a utf-8 byte array
     */
    public static byte[] asCharBytes(byte[] bytes, int index) {
        byte b = bytes[index];
        return switch (followsCount(b)) {
            case 1 -> new byte[] { b };
            case 2 -> new byte[] { b, bytes[index + 1] };
            case 3 -> new byte[] { b, bytes[index + 1], bytes[index + 2] };
            case 4 -> new byte[] { b, bytes[index + 1], bytes[index + 2], bytes[index + 3] };
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }


    /**
     * Get the utf-8 byte array from the code point value.
     * @param cp the code point value
     * @return the utf-8 byte array
     */
    public static byte[] fromCodePoint(int cp) {

        int mask = 0x3F; // 6bit mask 0011 1111

        if (0x0000 <= cp && cp <= 0x007f) {
            return new byte[] { (byte) cp };

        } else if (0x0080 <= cp && cp <= 0x07ff) {
            return new byte[] {
                (byte) (0xC0 | (cp >>> 6)),
                (byte) (0x80 | (cp & mask)) };

        } else if (0x0800 <= cp && cp <= 0xffff) {
            return new byte[] {
                (byte) (0xE0 | (cp >>> 12)),
                (byte) (0x80 | (cp >>>  6 & mask)),
                (byte) (0x80 | (cp & mask)) };

        } else if (0x10000 <= cp && cp <= 0x10ffff) {
            return new byte[] {
                (byte) (0xF0 | (cp >>> 18)),
                (byte) (0x80 | (cp >>> 12 & mask)),
                (byte) (0x80 | (cp >>>  6 & mask)),
                (byte) (0x80 | (cp & mask)) };
        } else {
            throw new IllegalStateException("Illegal code point. " + cp);
        }

    }


    /**
     * Get the utf-8 charset.
     * @return the utf-8 charset
     */
    public static Charset charset() {
        return StandardCharsets.UTF_8;
    }

}
