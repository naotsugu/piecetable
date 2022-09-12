package com.mammb.code.piecetable.array;

import java.io.Serializable;
import java.util.Arrays;

public class ByteArray implements Serializable {

    private static final byte[] EMPTY = {};

    private byte[] values;
    private int length;

    private ByteArray(byte[] values, int length) {
        this.values = values;
        this.length = length;
    }

    public static ByteArray of() {
        return new ByteArray(EMPTY, 0);
    }

    public static ByteArray of(byte[] bytes) {
        return new ByteArray(Arrays.copyOf(bytes, bytes.length), bytes.length);
    }

    public void add(byte val) {
        if (length == values.length) {
            values = grow(length + 1);
        }
        values[length++] = val;
    }

    public void add(byte[] bytes) {
        if (length + bytes.length > values.length) {
            values = grow(bytes.length);
        }
        System.arraycopy(bytes, 0, values, length, bytes.length);
        length += bytes.length;
    }

    public byte get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        return values[index];
    }

    public byte[] get() {
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

    private byte[] grow(int minCapacity) {
        int oldCapacity = values.length;
        if (length == 0 || values == EMPTY) {
            return values = new byte[Math.max(10, minCapacity)];
        } else {
            return values = Arrays.copyOf(values, ArraySupport.newCapacity(oldCapacity,
                    minCapacity - oldCapacity,
                    Math.min(512, oldCapacity >> 1)));
        }
    }

}
