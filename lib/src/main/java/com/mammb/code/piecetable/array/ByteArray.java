package com.mammb.code.piecetable.array;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Growable byte array.
 * @author Naotsugu Kobayashi
 */
public class ByteArray implements Serializable {

    private static final byte[] EMPTY = {};

    private byte[] bytes;
    private int length;

    private ByteArray(byte[] bytes, int length) {
        this.bytes = bytes;
        this.length = length;
    }

    public static ByteArray of() {
        return new ByteArray(EMPTY, 0);
    }

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
