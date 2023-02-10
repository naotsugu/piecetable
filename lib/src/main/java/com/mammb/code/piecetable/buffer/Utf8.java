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
package com.mammb.code.piecetable.buffer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The utility of UTF-8.
 * @author Naotsugu Kobayashi
 */
public abstract class Utf8 {

    private Utf8() { }

    public static boolean isSurrogateRetain(byte b) {
        // 10.. ....
        return (b & 0xC0) == 0x80;
    }

    public static short surrogateCount(byte b) {
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

    public static byte[] asCharBytes(byte[] bytes, int index) {
        byte b = bytes[index];
        return switch (surrogateCount(b)) {
            case 1 -> new byte[] { b };
            case 2 -> new byte[] { b, bytes[index + 1] };
            case 3 -> new byte[] { b, bytes[index + 1], bytes[index + 2] };
            case 4 -> new byte[] { b, bytes[index + 1], bytes[index + 2], bytes[index + 3] };
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    public static Charset charset() {
        return StandardCharsets.UTF_8;
    }

}
