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
package com.mammb.dev.picetable.core;

import java.io.Serializable;
import java.util.Arrays;

public class ByteArray implements Serializable {

    /** The empty byte array. */
    private static final byte[] EMPTY = {};

    /** The byte array. */
    private byte[] bytes;

    /** The length of array. */
    private int length;


    /**
     * Create a new {@code ByteArray}.
     * @param bytes the source byte array
     * @param length the length of array
     */
    private ByteArray(byte[] bytes, int length) {
        this.bytes = bytes;
        this.length = length;
    }


    /**
     * Create a new {@code ByteArray}.
     * @return a new {@code ByteArray}
     */
    public static ByteArray of() {
        return new ByteArray(EMPTY, 0);
    }


    /**
     * Create a new {@code ByteArray} from the given byte array.
     * @param bytes the given byte array
     * @return a new {@code ByteArray}
     */
    public static ByteArray of(byte[] bytes) {
        return new ByteArray(Arrays.copyOf(bytes, bytes.length), bytes.length);
    }


    /**
     * Add byte value to this array.
     * @param value byte value to be added
     */
    public void add(byte value) {
        if (length == bytes.length) {
            bytes = grow(length + 1);
        }
        bytes[length++] = value;
    }


    /**
     * Add byte array to this array.
     * @param values byte array to be added
     */
    public void add(byte[] values) {
        if (length + values.length > bytes.length) {
            this.bytes = grow(length + values.length);
        }
        System.arraycopy(values, 0, bytes, length, values.length);
        length += values.length;
    }


    /**
     * Get byte at the specified index position.
     * @param index the specified index position
     * @return byte value
     */
    public byte get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length));
        }
        return bytes[index];
    }


    /**
     * Get the copies of byte array.
     * @return the copies of byte array
     */
    public byte[] get() {
        return Arrays.copyOf(bytes, length);
    }


    /**
     * Get copies the specified range of this array.
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive
     * @return a new array containing the specified range from the original array
     */
    public byte[] get(int from, int to) {
        if (from < 0 || to > length || from > to) {
            throw new IndexOutOfBoundsException(
                "from[%d], to[%d], length[%d]".formatted(from, to, length));
        }
        return Arrays.copyOfRange(bytes, from, to);
    }


    /**
     * Get the new reversed {@code ByteArray}.
     * @return the new reversed {@code ByteArray}
     */
    public ByteArray reverse() {
        byte[] ret = get();
        for(int i = 0; i < ret.length / 2; i++) {
            byte temp = ret[i];
            ret[i] = ret[ret.length - i - 1];
            ret[ret.length - i - 1] = temp;
        }
        return new ByteArray(ret, length);
    }


    /**
     * Clear this array.
     */
    public void clear() {
        bytes = EMPTY;
        length = 0;
    }


    /**
     * Get the length of byte array.
     * @return the length of byte array
     */
    public int length() {
        return length;
    }


    /**
     * Get the capacity of byte array.
     * @return the capacity of byte array
     */
    public int capacity() {
        return bytes.length;
    }


    /**
     * Grow this byte array buffer.
     * @param minCapacity the growth capacity
     * @return the grown byte array
     */
    private byte[] grow(int minCapacity) {
        int oldCapacity = bytes.length;
        if (length == 0 || bytes == EMPTY) {
            return bytes = new byte[Math.max(10, minCapacity)];
        } else {
            return bytes = Arrays.copyOf(bytes, newCapacity(oldCapacity,
                minCapacity - oldCapacity,
                Math.min(512, oldCapacity >> 1)));
        }
    }

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
            if (minLength < 0) {
                throw new OutOfMemoryError(
                    "Required array length %d %d is too large".formatted(oldLength, minGrowth));
            }
            return Math.max(minLength, SOFT_MAX_ARRAY_LENGTH);
        }
    }
}
