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
 * The utf-16LE {@link CharsetMatch}.
 * @author Naotsugu Kobayashi
 */
class Utf16LEMatch implements CharsetMatch {

    /** The logger. */
    private static final System.Logger log = System.getLogger(Utf16LEMatch.class.getName());
    /** The result. */
    private final CharsetMatchResult result = new CharsetMatchResult(StandardCharsets.UTF_16LE);
    /** The sum length. */
    private long sumLength;

    @Override
    public Result put(byte[] bytes) {

        for (int i = 0; i < bytes.length - 1; i += 2) {
            int codeUnit = ((bytes[i + 1] & 0xff) << 8) | (bytes[i] & 0xff);
            if (i == 0 && sumLength == 0 && codeUnit == 0xFEFF) {
                result.exact();
                break;
            }
            if (codeUnit == 0) {
                result.decreasesConfidence();
            } else if ((codeUnit >= 0x20 && codeUnit <= 0xff) || codeUnit == 0x0a) {
                result.increasesConfidence();
            }
            if (result.confidence() == 100) break;
        }
        sumLength += bytes.length;
        log.log(DEBUG, result);
        return result;
    }

}
