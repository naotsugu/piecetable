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
 * The charset match.
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
        return _ -> Result.fixedOf(charset);
    }

    /**
     * The {@link CharsetMatch} result.
     */
    interface Result extends Comparable<Result> {

        /**
         * Get the charset.
         * @return the charset
         */
        Charset charset();

        /**
         * Get the confidence.
         * @return the confidence
         */
        int confidence();

        /**
         * Get the miss.
         * @return the miss
         */
        int miss();

        /**
         * Get if this result is vague.
         * @return {@code true} if this result is vague
         */
        boolean isVague();

        /**
         * Get the miss rank.
         * @return the miss rank
         */
        default int missRank() {
            return -1 * miss();
        }

        @Override
        default int compareTo(Result o) {
            return Comparator.comparingInt(Result::missRank)
                .thenComparing(Result::confidence)
                .compare(this, o);
        }

        /**
         * Create a new Fixed {@link Result}.
         * @param charset the charset
         * @return a new Fixed {@link Result}
         */
        static Result fixedOf(Charset charset) {
            record Fixed(Charset charset) implements Result {
                @Override public int confidence() { return 100; }
                @Override public int miss() { return 0; }
                @Override public boolean isVague() { return false; }
            }
            return new Fixed(charset);
        }
    }

}
