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
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.CharsetMatch;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The CharsetMatch simple implementation collections.
 * @author Naotsugu Kobayashi
 */
public abstract class CharsetMatches {


    private CharsetMatches() {
    }

    /**
     * Create a new utf-8 {@link CharsetMatch}.
     * @return a new utf-8 {@link CharsetMatch}
     */
    static CharsetMatch utf8() {
        return new Utf8Match();
    }

    /**
     * Create a new ms932 {@link CharsetMatch}.
     * @return a new ms932 {@link CharsetMatch}
     */
    static CharsetMatch ms932() {
        return new Ms932Match();
    }

    /**
     * The utf-8 {@link CharsetMatch}.
     */
    private static class Utf8Match implements CharsetMatch {

        private int confidence = 50;
        private int trail = 0;
        private int miss = 0;

        @Override
        public Result put(byte[] bytes) {

            for (int i = 0; i < bytes.length; i++) {

                if (confidence <= 0 || confidence >= 100) break;

                if (trail == 0) {
                    byte b = bytes[i];
                    trail = trail(b);
                    if (trail == -1) {
                        confidence = clamp(--confidence);
                        miss++;
                    }
                    if (trail <= 0) continue;
                }

                for (;;) {
                    i++;
                    if (i >= bytes.length) {
                        break;
                    }
                    byte b = bytes[i];
                    if ((b & 0xc0) != 0x80) {
                        confidence = clamp(--confidence);
                        miss++;
                        trail = 0;
                        break;
                    }
                    if (--trail == 0) {
                        confidence = clamp(++confidence);
                        break;
                    }
                }
            }
            return new Result(StandardCharsets.UTF_8, clamp(confidence - miss));
        }

        private int trail(byte b) {
            if ((b & 0x80) == 0x00) {
                return 0; // 0... ....  ASCII
            } else if ((b & 0xE0) == 0xC0) {
                return 1; // 110. ....
            } else if ((b & 0xF0) == 0xE0) {
                return 2; // 1110 ....
            } else if ((b & 0xF8) == 0xF0) {
                return 3; // 1111 0...
            } else {
                System.out.println(Byte.toString(b));
                return -1;
            }
        }
    }

    /**
     * The ms932 {@link CharsetMatch}.
     */
    private static class Ms932Match implements CharsetMatch {
        private int confidence = 50;
        private int trail = 0;
        private int miss = 0;
        @Override
        public Result put(byte[] bytes) {
            for (int i = 0; i < bytes.length; i++) {
                int b = Byte.toUnsignedInt(bytes[i]);
                if (b == 0x80 || b == 0xa0 || b >= 0xfd) {
                    // unused
                    confidence = clamp(--confidence);
                    miss++;
                    continue;
                }
                if ((0x81 <= b && b <= 0x9f) || b >= 0xe0) {
                    // double width
                    trail = 1;
                    if (i + 1 >= bytes.length) {
                        break;
                    }

                    int s = Byte.toUnsignedInt(bytes[++i]);
                    if ((0x40 <= s && s <= 0x7e) || (0x80 <= s && s <= 0xfc)) {
                        confidence = clamp(++confidence);
                    } else {
                        confidence = clamp(--confidence);
                        miss++;
                    }
                    trail = 0;
                }
            }
            return new Result(Charset.forName("windows-31j"), clamp(confidence - miss));
        }
    }

    private static int clamp(int value) {
        return Math.min(100, Math.max(value, 0));
    }

}
