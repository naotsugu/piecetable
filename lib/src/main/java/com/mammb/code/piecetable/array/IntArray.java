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

    /** The empty int array. */
    private static final int[] EMPTY = {};

    /** The int array. */
    private int[] ints;

    /** The length of array. */
    private int length;


    /**
     * Create a new {@code IntArray}.
     * @param ints the source int array
     * @param length the length of array
     */
    private IntArray(int[] ints, int length) {
        this.ints = ints;
        this.length = length;
    }


    /**
     * Create a new {@code IntArray}.
     * @return a new {@code IntArray}
     */
    public static IntArray of() {
        return new IntArray(EMPTY, 0);
    }


    /**
     * Create a new {@code IntArray} from the given int value.
     * @param value the given int value
     * @return a new {@code IntArray}
     */
    public static IntArray of(int value) {
        return new IntArray(new int[]{ value }, 1);
    }


    /**
     * Create a new {@code IntArray} from the given int array.
     * @param values the given int array
     * @return a new {@code ByteArray}
     */
    public static IntArray of(int[] values) {
        return new IntArray(Arrays.copyOf(values, values.length), values.length);
    }


    /**
     * Add int value to this array.
     * @param value byte value to be added
     */
    public void add(int value) {
        if (length == ints.length) {
            ints = grow(length + 1);
        }
        ints[length++] = value;
    }


    /**
     * Add int array to this array.
     * @param values byte array to be added
     */
    public void add(int[] values) {
        if (length + values.length > ints.length) {
            ints = grow(length + values.length);
        }
        System.arraycopy(values, 0, ints, length, values.length);
        length += values.length;
    }


    /**
     * Get int at the specified index position.
     * @param index the specified index position
     * @return int value
     */
    public int get(int index) {
        return ints[index];
    }


    /**
     * Get the copies of int array.
     * @return the copies of int array
     */
    public int[] get() {
        return Arrays.copyOf(ints, length);
    }


    /**
     * Clear this array.
     */
    public void clear() {
        ints = EMPTY;
        length = 0;
    }


    /**
     * Get the length of int array.
     * @return the length of int array
     */
    public int length() {
        return length;
    }


    /**
     * Get the capacity of int array.
     * @return the capacity of int array
     */
    public int capacity() {
        return ints.length;
    }


    /**
     * Grow this int array buffer.
     * @param minCapacity the growth capacity
     * @return the grown byte array
     */
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
