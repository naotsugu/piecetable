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
package com.mammb.code.piecetable;

import java.nio.charset.Charset;
import java.util.Comparator;

/**
 * The CharsetMatch.
 * Estimates the character set from a given byte array.
 * @author Naotsugu Kobayashi
 */
public interface CharsetMatch {

    /**
     * Put the byte array.
     * @param bytes the byte array
     * @return the match result
     */
    Result put(byte[] bytes);


    /**
     * Create a fixed charset {@link CharsetMatch}.
     * @param charset the charset
     * @return a new {@link CharsetMatch}
     */
    static CharsetMatch of(Charset charset) {
        return bytes -> new Result(charset, 100, 0);
    }


    /**
     * The {@link CharsetMatch} result.
     * @param charset the charset
     * @param confidence the confidence
     * @param miss the miss
     */
    record Result(Charset charset, int confidence, int miss) implements Comparable<Result> {
        @Override
        public int compareTo(Result o) {
            return Comparator.comparingInt(Result::miss)
                .thenComparing(Result::confidence)
                .compare(o, this);
        }
    }

}
