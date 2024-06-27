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
package com.mammb.code.piecetable.array;

/**
 * Array utility.
 * @author Naotsugu Kobayashi
 */
public class ArraySupport {

    /** The max length of array. */
    private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;


    /**
     * Create new capacity size.
     * @param oldLength the old length
     * @param minGrowth the min growth
     * @param prefGrowth the pref growth
     * @return the new capacity size
     */
    public static int newCapacity(int oldLength, int minGrowth, int prefGrowth) {
        int prefLength = oldLength + Math.max(minGrowth, prefGrowth);
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            int minLength = oldLength + minGrowth;
            if (minLength < 0) { // overflow
                throw new OutOfMemoryError(
                    "Required array length %d %d is too large".formatted(oldLength, minGrowth));
            }
            return Math.max(minLength, SOFT_MAX_ARRAY_LENGTH);
        }
    }

}
