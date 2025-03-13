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
package com.mammb.code.piecetable.text;

import java.util.Arrays;

/**
 * The RowIndex.
 * Holds the byte length of each row as an index.
 * <pre>
 *     |0|1|2|3|4|5|
 *  0|  a b $         rowLengths[0] = 3     stCache[0] = 0
 *  1|  c d e $       rowLengths[1] = 4           ^
 *  2|  f g h i $     rowLengths[2] = 5           |
 *  3|  1 $           rowLengths[3] = 2      cacheInterval = 5
 *  4|  1 2 $         rowLengths[4] = 3           |
 *  5|  1 2 3 $       rowLengths[5] = 4     stCache[1] = 17
 * </pre>
 * @author Naotsugu Kobayashi
 */
public class RowIndex {

    /** The row lengths. */
    private int[] rowLengths;
    /** The length of row lengths array. */
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
     * @param prefRows the pref row size
     */
    private RowIndex(int cacheInterval, int prefRows) {
        rowLengths = new int[Math.max(1, prefRows)];
        length = 1;

        stCache = new long[Math.max(1, prefRows / cacheInterval)];
        cacheLength = 1;
        this.cacheInterval = cacheInterval;
    }

    /**
     * Create a new {@link RowIndex}.
     * @return a new {@link RowIndex}
     */
    public static RowIndex of() {
        return new RowIndex(100, 0);
    }

    /**
     * Create a new {@link RowIndex}.
     * @param prefRows the pref row size
     * @return a new {@link RowIndex}
     */
    public static RowIndex of(int prefRows) {
        return new RowIndex(100, prefRows);
    }

    /**
     * Create a new {@link RowIndex}.
     * @param cacheInterval the sub-total cache interval
     * @param prefRows the pref row size
     * @return a new {@link RowIndex}
     */
    static RowIndex of(int cacheInterval, int prefRows) {
        return new RowIndex(cacheInterval, prefRows);
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
     * Gets the total byte length of the specified row from the head.
     * <pre>
     *   0  | * | * |
     *   2  | * | * |
     *   4  | * | * |
     * </pre>
     * @param row the specified row
     * @return the total byte length of the specified row from the head
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

            // insert operation across multiple rows
            int head = col + rows[0];
            int tail = (rowLengths[row] - col) + rows[rows.length - 1];

            System.arraycopy(rowLengths, row + 1,
                rowLengths, row + rows.length,
                length - (row + 1));

            rowLengths[row] = head;
            System.arraycopy(rows, 1, rowLengths, row + 1, rows.length - 1 - 1);
            rowLengths[row + rows.length - 1] = tail;

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
                if ((row + lines + 1) >= length) break;
                len -= rowLengths[row + ++lines];
            } while (len >= 0);

            rowLengths[row] += (-len); // merge the rest to the first row

            if (lines > 0) {
                System.arraycopy(
                    rowLengths, row + 1 + lines,
                    rowLengths, row + 1,
                    length - (row + 1 + lines));
                length -= lines;
            }
        }
    }

    /**
     * Trim to size.
     */
    public void trimToSize() {
        if (Math.max(1, length) < rowLengths.length) {
            rowLengths = Arrays.copyOf(rowLengths, Math.max(1, length));
        }
        if (Math.max(1, cacheLength) < stCache.length) {
            stCache = Arrays.copyOf(stCache, Math.max(1, cacheLength));
        }
    }

    /**
     * Get the size of rows.
     * @return the size of rows
     */
    public int rowSize() {
        return length;
    }

    /**
     * Get the serial position.
     * Length of BOM is excluded.
     * @param row the specified row
     * @param col the specified position in a row
     * @return the serial position
     */
    public long serial(int row, int col) {
        return get(row) + Math.min(rowLengths[row], col);
    }

    /**
     * Get the row-col position.
     * @param serial the serial position
     * @return the row-col position
     */
    public int[] pos(long serial) {
        int result = Arrays.binarySearch(stCache, 0, cacheLength, serial);
        if (result >= 0) {
            return new int[] { result * cacheInterval, 0 };
        } else {
            int point = Math.max(~result - 1, 0);
            long st = stCache[point];
            for (int i = point * cacheInterval; i < length; i++) {
                int len = rowLengths[i];
                if (st + len > serial) {
                    int col = (int) (serial - st);
                    return new int[] { i, col };
                } else {
                    st += len;
                }
            }
            return new int[] { length - 1, rowLengths[length - 1] };
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
                Math.max(minCapacity, oldCapacity + (oldCapacity >> 1)),
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
                Math.max(minCapacity, oldCapacity + (oldCapacity >> 2)),
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
