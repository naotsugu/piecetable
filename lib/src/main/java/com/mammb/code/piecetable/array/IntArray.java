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
 * Growable int array.
 * @author Naotsugu Kobayashi
 */
public class IntArray implements Serializable {

    private static final int[] EMPTY = {};

    private int[] ints;
    private int length;

    private IntArray(int[] ints, int length) {
        this.ints = ints;
        this.length = length;
    }

    public static IntArray of() {
        return new IntArray(EMPTY, 0);
    }

    public static IntArray of(int value) {
        return new IntArray(new int[]{ value }, 1);
    }

    public static IntArray of(int[] values) {
        return new IntArray(Arrays.copyOf(values, values.length), values.length);
    }

    public void add(int[] values) {
        if (length + values.length > ints.length) {
            ints = grow(length + values.length);
        }
        System.arraycopy(values, 0, ints, length, values.length);
        length += values.length;
    }

    public void add(int value) {
        if (length == ints.length) {
            ints = grow(length + 1);
        }
        ints[length++] = value;
    }

    public int get(int index) {
        return ints[index];
    }

    public int[] get() {
        return Arrays.copyOf(ints, length);
    }

    public void clear() {
        ints = EMPTY;
        length = 0;
    }

    public int length() {
        return length;
    }

    public int capacity() {
        return ints.length;
    }

    private int[] grow(int minCapacity) {
        int oldCapacity = ints.length;
        if (length == 0 || ints == EMPTY) {
            return ints = new int[Math.max(10, minCapacity)];
        } else {
            return ints = Arrays.copyOf(ints, ArraySupport.newCapacity(oldCapacity,
                    minCapacity - oldCapacity,
                    Math.min(64, oldCapacity >> 1)));
        }
    }

}
