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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The {@link CharsetMatch} utilities.
 * @author Naotsugu Kobayashi
 */
public interface CharsetMatches {

    /**
     * Create a new default {@link CharsetMatch}.
     * @return a new default {@link CharsetMatch}
     */
    static CharsetMatch[] defaults() {
        return new CharsetMatch[] { utf8(), ms932(),
            new Utf16BEMatch(), new Utf16LEMatch() };
    }

    /**
     * Create the {@link CharsetMatch}.
     * @return the {@link CharsetMatch}
     */
    static CharsetMatch[] all() {
        return new CharsetMatch[] { utf8(), ms932(),
            new Utf16BEMatch(), new Utf16LEMatch(),
            new Utf32BEMatch(), new Utf32LEMatch() };
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
     * Get the estimated {@link Charset}.
     * @param bytes the specified bytes
     * @param matches the list of {@link CharsetMatch}
     * @return the estimated {@link Charset}
     */
    static Optional<Charset> estimate(byte[] bytes, List<CharsetMatch> matches) {
        var result = matches.stream()
            .map(m -> m.put(bytes))
            .max(Comparator.naturalOrder());
        return (result.isEmpty() || result.get().isVague())
            ? Optional.empty()
            : result.map(CharsetMatch.Result::charset);
    }

    /**
     * Get the estimated {@link Charset}.
     * @param bytes the specified bytes
     * @param matches the array of {@link CharsetMatch}
     * @return the estimated {@link Charset}
     */
    static Optional<Charset> estimate(byte[] bytes, CharsetMatch... matches) {
        return estimate(bytes, List.of(matches));
    }

}
