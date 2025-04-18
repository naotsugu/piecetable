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
                result.decrement();
                continue;
            }
            if ((0x81 <= b && b <= 0x9f) || b >= 0xe0) {
                // double width
                if (i + 1 >= bytes.length) {
                    break;
                }

                int s = Byte.toUnsignedInt(bytes[++i]);
                if ((0x40 <= s && s <= 0x7e) || (0x80 <= s && s <= 0xfc)) {
                    result.increment();
                } else {
                    result.decrement();
                }
            }
        }
        log.log(DEBUG, result);
        return result;
    }

}
