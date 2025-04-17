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
 * The ms932 {@link CharsetMatch}.
 * @author Naotsugu Kobayashi
 */
class Ms932Match implements CharsetMatch {

    /** logger. */
    private static final System.Logger log = System.getLogger(Ms932Match.class.getName());

    private int confidence = 50;
    private int trail = 0;
    private int miss = 0;

    @Override
    public Result put(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int b = Byte.toUnsignedInt(bytes[i]);
            if (b == 0x80 || b == 0xa0 || b >= 0xfd) {
                // unused
                confidence = Math.clamp(--confidence, 0, 100);
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
                    confidence = Math.clamp(++confidence, 0, 100);
                } else {
                    confidence = Math.clamp(--confidence, 0, 100);
                    miss++;
                }
                trail = 0;
            }
        }
        log.log(DEBUG, "windows-31j confidence:{0}, miss:{1}", confidence, miss);
        return new Result(Charset.forName("windows-31j"), Math.clamp(confidence, 0, 100), miss);
    }

}
