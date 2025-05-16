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
import java.nio.charset.Charset;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * This class implements the {@link CharsetMatch} interface to estimate the validity of the
 * MS932 (Shift-JIS for Windows) charset encoding for a given byte array.
 *
 * The class analyzes the input bytes to determine whether the bytes conform to the
 * expected pattern of MS932 encoded data. The result is represented through a confidence
 * score, which reflects the likelihood of the input conforming to the MS932 encoding.
 *
 * The confidence level is adjusted based on the analysis of individual bytes and pairs
 * of bytes. Invalid byte sequences or characters reduce the confidence, while valid sequences
 * enhance it. Certain patterns and ranges of characters are identified as invalid, and these
 * are implemented in the {@link #isInvalid(int, int)} method.
 *
 * Internal confidence adjustments work in conjunction with the {@link CharsetMatchResult}
 * class, which manages the state of confidence, miss counts, and other properties related
 * to charset matching.
 * @author Naotsugu Kobayashi
 */
class Ms932Match implements CharsetMatch {

    /** The logger. */
    private static final System.Logger log = System.getLogger(Ms932Match.class.getName());
    /** The result. */
    private final CharsetMatchResult result = new CharsetMatchResult(Charset.forName("windows-31j"));

    @Override
    public Result put(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int b = Byte.toUnsignedInt(bytes[i]);
            if (b == 0x80 || b == 0xa0 || b >= 0xfd) {
                // unused
                result.decreasesConfidence();
                continue;
            }
            if ((0x81 <= b && b <= 0x9f) || 0xe0 <= b && b <= 0xfc) {
                // double width
                if (i + 1 >= bytes.length) {
                    break;
                }

                int s = Byte.toUnsignedInt(bytes[++i]);
                if ((0x40 <= s && s <= 0x7e) || (0x80 <= s && s <= 0xfc)) {
                    if (isInvalid(b, s)) {
                        result.decreasesConfidence();
                    } else {
                        result.increasesConfidence();
                    }
                } else {
                    result.decreasesConfidence();
                }
            }
        }
        log.log(DEBUG, result);
        return result;
    }

    boolean isInvalid(int hi, int lo) {
        if (hi == 0x81) {
            return 0xad <= lo && lo <= 0xb7 || 0xc0 <= lo && lo <= 0xc7
                || 0xcf <= lo && lo <= 0xd9 || 0xe9 <= lo && lo <= 0xef
                || 0xf8 <= lo && lo <= 0xfb;
        } else if (hi == 0x82) {
            return 0x40 <= lo && lo <= 0x4e || 0x59 <= lo && lo <= 0x5f
                || 0x7a <= lo && lo <= 0x80 || 0x9b <= lo && lo <= 0x9e
                || 0xf2 <= lo;
        } else if (hi == 0x83) {
            return 0x97 <= lo && lo <= 0x9e || 0xb7 <= lo && lo <= 0xbe
                || 0xd7 <= lo;
        } else if (hi == 0x84) {
            return 0x61 <= lo && lo <= 0x6f || 0x92 <= lo && lo <= 0x9e
                || 0xdf <= lo;
        } else if (hi == 0x85) {
            return true;
        } else if (hi == 0x86) {
            return true;
        } else if (hi == 0x87) {
            return 0x5e == lo || 0x76 <= lo && lo <= 0x7d || 0x9d <= lo;
        } else if (hi == 0x88) {
            return 0x40 <= lo && lo <= 0x9e;
        } else if (hi == 0x98) {
            return 0x73 <= lo && lo <= 0x9e;
        } else if (hi == 0xea) {
            return 0xa5 <= lo;
        } else if (hi == 0xeb) {
            return true;
        } else if (hi == 0xec) {
            return true;
        } else if (hi == 0xee) {
            return 0xed <= lo && lo <= 0xee;
        } else if (hi == 0xef) {
            return true;
        } else {
            return false;
        }
    }

}
