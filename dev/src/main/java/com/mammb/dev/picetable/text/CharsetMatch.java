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
package com.mammb.dev.picetable.text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The CharsetMatch.
 * @author Naotsugu Kobayashi
 */
public interface CharsetMatch {

    Result put(byte[] bytes);


    static CharsetMatch of(Charset charset) {
        return bytes -> new Result(charset, 100);
    }


    static CharsetMatch utf8() {
        return new Utf8Match();
    }


    record Result(Charset charset, int confidence) implements Comparable<Result> {
        @Override
        public int compareTo(Result o) {
            return Integer.compare(confidence, o.confidence);
        }
    }

    class Utf8Match implements CharsetMatch {

        private int confidence = 50;
        private int trail = 0;

        @Override
        public Result put(byte[] bytes) {

            for (int i = 0; i < bytes.length; i++) {

                if (confidence <= 0 || confidence >= 100) break;

                if (trail == 0) {
                    byte b = bytes[i];
                    int trail = trail(b);
                    if (trail == -1) confidence = clamp(confidence--);
                    if (trail <= 0) continue;
                }

                for (;;) {
                    i++;
                    if (i >= bytes.length) {
                        break;
                    }
                    byte b = bytes[i];
                    if ((b & 0xc0) != 0x080) {
                        confidence = clamp(confidence--);
                        trail = 0;
                        break;
                    }
                    if (--trail == 0) {
                        confidence = clamp(confidence++);
                        break;
                    }
                }
            }
            return new Result(StandardCharsets.UTF_8, confidence);
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
                return -1;
            }
        }
    }

    class SjisMatch implements CharsetMatch {
        private int confidence = 50;
        @Override
        public Result put(byte[] bytes) {
            return new Result(Charset.forName("windows-31j"), confidence);
        }
    }

    private static int clamp(int value) {
        return Math.min(100, Math.max(value, 0));
    }

}
