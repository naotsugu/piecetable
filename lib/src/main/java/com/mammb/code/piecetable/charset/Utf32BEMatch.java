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
 * Utf32BEMatch is an implementation of the CharsetMatch interface, designed to estimate
 * the character set of input byte arrays using the UTF-32BE charset. It assesses the validity
 * and compatibility of byte data with UTF-32BE encoding, adjusting confidence based on the
 * characteristics of the input data.
 * @author Naotsugu Kobayashi
 */
public class Utf32BEMatch implements CharsetMatch {

    /** The logger. */
    private static final System.Logger log = System.getLogger(Utf32BEMatch.class.getName());
    /** The result. */
    private final CharsetMatchResult result = new CharsetMatchResult(StandardCharsets.UTF_32BE);
    /** The sum length. */
    private long sumLength;

    /**
     * Constructor.
     */
    public Utf32BEMatch() {
    }

    @Override
    public Result put(byte[] bytes) {

        int limit = (bytes.length / 4) * 4;

        if (limit == 0) {
            return result;
        }

        if (sumLength == 0 && getChar(bytes, 0) == 0x0000FEFF) {
            result.exact();
            return result;
        }

        for (int i = 0; i < limit; i += 4) {
            int ch = getChar(bytes, i);
            if (ch < 0 || ch >= 0x10FFFF || (ch >= 0xD800 && ch <= 0xDFFF)) {
                result.decreasesConfidence();
            } else {
                result.increasesConfidence();
            }
        }
        sumLength += bytes.length;
        log.log(DEBUG, result);
        return result;
    }

    private static int getChar(byte[] bytes, int index) {
        return (bytes[index] & 0xFF) << 24 | (bytes[index + 1] & 0xFF) << 16 |
               (bytes[index + 2] & 0xFF) <<  8 | (bytes[index + 3] & 0xFF);
    }
}
