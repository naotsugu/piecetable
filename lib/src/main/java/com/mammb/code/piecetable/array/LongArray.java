package com.mammb.code.piecetable.array;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Growable long array.
 * @author Naotsugu Kobayashi
 */
public class LongArray implements Serializable {

    /** The empty long array. */
    private static final long[] EMPTY = {};

    /** The long array. */
    private long[] longs;

    /** The length of array. */
    private int length;


    /**
     * Create a new {@code LongArray}.
     * @param longs the source int array
     * @param length the length of array
     */
    private LongArray(long[] longs, int length) {
        this.longs = longs;
        this.length = length;
    }


    /**
     * Create a new {@code LongArray}.
     * @return a new {@code LongArray}
     */
    public static LongArray of() {
        return new LongArray(EMPTY, 0);
    }


    /**
     * Create a new {@code LongArray} with the initial capacity.
     * @param initialCapacity the initial capacity
     * @return a new {@code LongArray}
     */
    public static LongArray of(int initialCapacity) {
        return new LongArray(new long[initialCapacity], 0);
    }


    /**
     * Create a new {@code LongArray} from the given int array.
     * @param values the given int array
     * @return a new {@code LongArray}
     */
    public static LongArray of(long[] values) {
        return new LongArray(Arrays.copyOf(values, values.length), values.length);
    }


    /**
     * Add int value to this array.
     * @param value byte value to be added
     */
    public void add(long value) {
        if (length == longs.length) {
            longs = grow(length + 1);
        }
        longs[length++] = value;
    }


    /**
     * Add long array to this array.
     * @param values byte array to be added
     */
    public void add(long[] values) {
        if (length + values.length > longs.length) {
            longs = grow(length + values.length);
        }
        System.arraycopy(values, 0, longs, length, values.length);
        length += values.length;
    }


    /**
     * Get int at the specified index position.
     * @param index the specified index position
     * @return int value
     */
    public long get(int index) {
        return longs[index];
    }


    /**
     * Get the copies of int array.
     * @return the copies of int array
     */
    public long[] get() {
        return Arrays.copyOf(longs, length);
    }


    /**
     * Clear this array.
     */
    public void clear() {
        longs = EMPTY;
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
        return longs.length;
    }


    /**
     * Grow this int array buffer.
     * @param minCapacity the growth capacity
     * @return the grown byte array
     */
    private long[] grow(int minCapacity) {
        int oldCapacity = longs.length;
        if (length == 0 || longs == EMPTY) {
            return longs = new long[Math.max(10, minCapacity)];
        } else {
            return longs = Arrays.copyOf(longs, ArraySupport.newCapacity(oldCapacity,
                minCapacity - oldCapacity,
                Math.min(256, oldCapacity >> 1)));
        }
    }

}
