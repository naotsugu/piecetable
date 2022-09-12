package com.mammb.code.piecetable.array;

import java.io.Serializable;
import java.util.Arrays;

public class IntArray implements Serializable {

    private static final int[] EMPTY = {};

    private int[] values;
    private int length;

    private IntArray(int[] values, int length) {
        this.values = values;
        this.length = length;
    }

    public static IntArray of() {
        return new IntArray(EMPTY, 0);
    }

    public static IntArray of(int[] ints) {
        return new IntArray(Arrays.copyOf(ints, ints.length), ints.length);
    }

    public void add(int[] ints) {
        if (length + ints.length > values.length) {
            values = grow(ints.length);
        }
        System.arraycopy(ints, 0, values, length, ints.length);
        length += ints.length;
    }

    public void add(int val) {
        if (length == values.length) {
            values = grow(length + 1);
        }
        values[length++] = val;
    }

    public int get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        return values[index];
    }

    public int[] get() {
        return Arrays.copyOf(values, length);
    }

    public void clear() {
        values = EMPTY;
        length = 0;
    }

    public int length() {
        return length;
    }

    public int capacity() {
        return values.length;
    }

    private int[] grow(int minCapacity) {
        int oldCapacity = values.length;
        if (length == 0 || values == EMPTY) {
            return values = new int[Math.max(10, minCapacity)];
        } else {
            return values = Arrays.copyOf(values, ArraySupport.newCapacity(oldCapacity,
                    minCapacity - oldCapacity,
                    Math.min(64, oldCapacity >> 1)));
        }
    }

}
