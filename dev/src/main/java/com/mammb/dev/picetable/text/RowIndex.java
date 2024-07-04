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
package com.mammb.dev.picetable.text;

import java.util.Arrays;

/**
 * The RowIndex.
 * Holds the byte length of each row as an index.
 * @author Naotsugu Kobayashi
 */
public class RowIndex {

    /** The row lengths. */
    private int[] rowLengths;
    /** The length of line lengths array. */
    private int length;

    /** The sub-total cache. */
    private long[] stCache;
    /** The sub-total cache length. */
    private int cacheLength;
    /** The sub-total cache interval. */
    private final int cacheInterval;


    /**
     * Create a new {@code RowIndex}.
     * @param cacheInterval the sub-total cache interval
     */
    private RowIndex(int cacheInterval) {
        rowLengths = new int[0];
        length = 0;

        stCache = new long[0];
        cacheLength = 0;
        this.cacheInterval = cacheInterval;
    }


    /**
     * Create a new {@link RowIndex}.
     * @return a new {@link RowIndex}
     */
    public static RowIndex of() {
        return new RowIndex(100);
    }


    /**
     * Create a new {@link RowIndex}.
     * @param cacheInterval the sub-total cache interval
     * @return a new {@link RowIndex}
     */
    static RowIndex of(int cacheInterval) {
        return new RowIndex(cacheInterval);
    }


    /**
     * Adds the specified byte array to the index.
     * @param bytes the specified byte array to be added
     */
    public void add(byte[] bytes) {

        int[] rows = rows(bytes);
        if (rows.length == 0) {
            return;
        }

        if (length + rows.length > rowLengths.length) {
            rowLengths = grow(length + rows.length);
        }

        if (length == 0) {
            length++;
        }
        for (int i = 0; i < rows.length; i++) {
            rowLengths[length - 1] += rows[i];
            if (rows.length > 1 && i < rows.length - 1) {
                // rows |0|
                // rows |0|length++|1|
                // rows |0|length++|1|length++|2|
                // rows |0|length++|1|length++|2|length++|3|
                length++;
            }
        }

    }


    /**
     * Gets the byte length of the specified row.
     * @param row the specified row
     * @return the byte length of the specified row
     */
    public long get(int row) {

        int startRow = 0;
        long startPos = 0;

        int cacheIndex = row / cacheInterval;
        if (cacheIndex > 0 && cacheIndex < cacheLength) {
            startRow = cacheIndex * cacheInterval;
            startPos = stCache[cacheIndex];
        }

        for (int i = startRow; i < length && i < row; i++) {
            if (i % cacheInterval == 0) {
                if (cacheLength + 1 > stCache.length) {
                    stCache = growCache(cacheLength + 1);
                }
                int chIndex = i / cacheInterval;
                stCache[chIndex] = startPos;
                cacheLength = chIndex + 1;
            }
            startPos += rowLengths[i];
        }
        return startPos;
    }


    /**
     * Insert the specified byte array to the index.
     * @param row the specified row
     * @param col the specified position in a row
     * @param bytes the specified byte array to be inserted
     */
    public void insert(int row, int col, byte[] bytes) {
        int[] rows = rows(bytes);
        if (rows.length == 0) {
            return;
        }

        if (length + rows.length > rowLengths.length) {
            rowLengths = grow(length + rows.length);
        }
        cacheLength = row / cacheInterval;

        if (rows.length == 1) {

            // insert operation within a single row
            rowLengths[row] += rows[0];

        } else {

            // insert operation across multiple rowss
            System.arraycopy(rowLengths, row + 1,
                rowLengths, row + rows.length,
                length - (row + 1));

            int tail = rowLengths[row] - col;
            rowLengths[row++] = col + rows[0];

            for (int i = 1; i < rows.length - 1; i++) {
                rowLengths[row++] = rows[i];
            }

            rowLengths[row] = tail + rows[rows.length - 1];

        }
        length += rows.length - 1;
    }


    /**
     * Delete the specified byte length of the index.
     * @param row the specified row
     * @param col the specified position in a row
     * @param len the specified byte length to be deleted
     */
    public void delete(int row, int col, int len) {

        if (len <= 0) {
            return;
        }

        cacheLength = row / cacheInterval;

        if ((rowLengths[row] - col) > len) {

            // delete operation within a single row
            // |a|b|c|d|$|       ->        |a|d|$|
            //   ^---  col:1, len:2
            rowLengths[row] -= len;

        } else {

            // delete operation across multiple rows
            // 0 |a|b|$|    delete(        ->   |a|f|$|
            // 1 |c|d|$|      row = 0,          |g|h|$|
            // 2 |e|f|$|      col = 1,
            // 3 |g|h|$|      len = 6)

            len -= rowLengths[row] - col;
            rowLengths[row] = col;
            int lines = 0;
            do {
                len -= rowLengths[row + ++lines];
            } while (len > 0);

            rowLengths[row] += (-len); // merge the rest to the first row

            System.arraycopy(
                rowLengths, row + 1 + lines,
                rowLengths, row + 1,
                length - (row + 1 + lines));
            length -= lines;
        }
    }


    /**
     * Converts the specified byte array to line-by-line byte length.
     * @param bytes the specified byte array
     * @return line-by-line byte length
     */
    static int[] rows(byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            return new int[0];
        }

        IntArray intArray = IntArray.of();

        int n = 0;
        for (byte aByte : bytes) {
            n++;
            if (aByte == '\n') {
                intArray.add(n);
                n = 0;
            }
        }
        intArray.add(n);

        return intArray.get();
    }


    /**
     * Grow this lineLengths array.
     * @param minCapacity the growth capacity
     * @return the grown int array
     */
    private int[] grow(int minCapacity) {
        int oldCapacity = rowLengths.length;
        if (oldCapacity > 0) {
            int newCapacity = Math.min(
                Math.max(minCapacity, oldCapacity >> 1),
                Integer.MAX_VALUE - 8);
            return rowLengths = Arrays.copyOf(rowLengths, newCapacity);
        } else {
            return rowLengths = new int[Math.max(100, minCapacity)];
        }
    }


    /**
     * Grow this sub-total cache array.
     * @param minCapacity the growth capacity
     * @return the grown long array
     */
    private long[] growCache(int minCapacity) {
        int oldCapacity = stCache.length;
        if (oldCapacity > 0) {
            int newCapacity = Math.min(
                Math.max(minCapacity, oldCapacity >> 2),
                Integer.MAX_VALUE - 8);
            return stCache = Arrays.copyOf(stCache, newCapacity);
        } else {
            return stCache = new long[Math.max(10, minCapacity)];
        }
    }


    /**
     * Gets the row lengths array.
     * @return the row lengths array
     */
    int[] rowLengths() {
        return Arrays.copyOf(rowLengths, length);
    }


    /**
     * Gets the sub-total cache array.
     * @return the sub-total cache array
     */
    long[] stCache() {
        return Arrays.copyOf(stCache, cacheLength);
    }

}
