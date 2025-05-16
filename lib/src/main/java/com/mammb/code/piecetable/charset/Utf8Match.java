/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.charset;

import com.mammb.code.piecetable.CharsetMatch;
import java.nio.charset.StandardCharsets;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * The Utf8Match class implements the CharsetMatch interface to analyze and estimate
 * if a given byte array matches the UTF-8 character set. It calculates confidence levels
 * based on the structure and sequence of bytes in the provided array.
 * @author Naotsugu Kobayashi
 */
class Utf8Match implements CharsetMatch {

    /** The logger. */
    private static final System.Logger log = System.getLogger(Utf8Match.class.getName());
    /** The result. */
    private final CharsetMatchResult result = new CharsetMatchResult(StandardCharsets.UTF_8);
    /** trail flag. */
    private int trail;

    @Override
    public Result put(byte[] bytes) {

        for (int i = 0; i < bytes.length; i++) {

            if (trail == 0) {
                byte b = bytes[i];
                trail = trail(b);
                if (trail == -1) {
                    result.decreasesConfidence();
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
                    result.decreasesConfidence();
                    trail = 0;
                    break;
                }
                if (--trail == 0) {
                    result.increasesConfidence();
                    break;
                }
            }
        }
        log.log(DEBUG, result);
        return result;
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
            log.log(DEBUG, "Bytes not expected[{}]", Byte.toString(b));
            return -1;
        }
    }

}
