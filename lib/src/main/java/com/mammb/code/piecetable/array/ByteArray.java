/*
 * Copyright 2019-2023 the original author or authors.
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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Growable byte array.
 * @author Naotsugu Kobayashi
 */
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

    public void add(byte value) {
        if (length == bytes.length) {
            bytes = grow(length + 1);
        }
        bytes[length++] = value;
    }

    public void add(byte[] values) {
        if (length + values.length > bytes.length) {
            this.bytes = grow(length + values.length);
        }
        System.arraycopy(values, 0, bytes, length, values.length);
        length += values.length;
    }

    public byte get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length));
        }
        return bytes[index];
    }

    public byte[] get() {
        return Arrays.copyOf(bytes, length);
    }

    public void fill(byte[] dest) {
        System.arraycopy(bytes, 0, dest, 0, Math.min(bytes.length, dest.length));
    }

    public byte[] get(int from, int to) {
        if (from < 0 || to > length || from > to) {
            throw new IndexOutOfBoundsException(
                "from[%d], to[%d], length[%d]".formatted(from, to, length));
        }
        return Arrays.copyOfRange(bytes, from, to);
    }

    public ByteArray reverse() {
        byte[] ret = get();
        for(int i = 0; i < ret.length / 2; i++) {
            byte temp = ret[i];
            ret[i] = ret[ret.length - i - 1];
            ret[ret.length - i - 1] = temp;
        }
        return new ByteArray(ret, length);
    }

    public void clear() {
        bytes = EMPTY;
        length = 0;
    }

    public int length() {
        return length;
    }

    public int capacity() {
        return bytes.length;
    }

    private byte[] grow(int minCapacity) {
        int oldCapacity = bytes.length;
        if (length == 0 || bytes == EMPTY) {
            return bytes = new byte[Math.max(10, minCapacity)];
        } else {
            return bytes = Arrays.copyOf(bytes, ArraySupport.newCapacity(oldCapacity,
                    minCapacity - oldCapacity,
                    Math.min(512, oldCapacity >> 1)));
        }
    }

}
