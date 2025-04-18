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

/**
 * The CharsetMatch simple implementation collections.
 * @author Naotsugu Kobayashi
 */
public interface CharsetMatches {

    /**
     * Create a new default {@link CharsetMatch}.
     * @return a new default {@link CharsetMatch}
     */
    static CharsetMatch[] defaults() {
        return new CharsetMatch[] { utf8(), ms932(), new Utf16BEMatch(), new Utf16LEMatch() };
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

}
