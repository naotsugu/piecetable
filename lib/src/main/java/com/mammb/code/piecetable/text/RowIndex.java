/*
 * Copyright 2022-2026 the original author or authors.
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

import java.nio.charset.Charset;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.*;

/**
 * The RowIndex.
 * This class represents a data structure that manages and tracks the row index
 * with detailed support for operations like adding, inserting, deleting, and
 * calculating lengths within rows. It also supports caching to optimize access to row data.
 * <p>
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
    /** The length of a row lengths array. */
    private int length;

    /** The subtotal cache. */
    private long[] stCache;
    /** The subtotal cache length. */
    private int cacheLength;
    /** The subtotal cache interval. */
    private final int cacheInterval;

    /** The byte width to reads. */
    private final int byteUnits;

    /**
     * Create a new {@code RowIndex}.
     * @param cacheInterval the subtotal cache interval
     * @param prefRows the pref row size
     * @param charset the charset
     */
    private RowIndex(int cacheInterval, int prefRows, Charset charset) {
        this.rowLengths = new int[Math.max(1, prefRows)];
        this.length = 1;

        this.stCache = new long[Math.max(1, prefRows / cacheInterval)];
        this.cacheLength = 1;
        this.cacheInterval = cacheInterval;

        this.byteUnits = (charset == null || UTF_8.equals(charset)) ? 1
            : (UTF_16.equals(charset) || UTF_16BE.equals(charset) || UTF_16LE.equals(charset)) ? 2
            : (UTF_32.equals(charset) || UTF_32BE.equals(charset) || UTF_32LE.equals(charset)) ? 4
            : 1;
    }

    /**
     * Create a new {@link RowIndex}.
     * @return a new {@link RowIndex}
     */
    public static RowIndex of() {
        return new RowIndex(100, 0, UTF_8);
    }

    /**
     * Creates a new {@link RowIndex} with the specified {@link Charset}.
     * @param charset the charset to be used in creating the {@link RowIndex}
     * @return a new {@link RowIndex} instance
     */
    public static RowIndex of(Charset charset) {
        return new RowIndex(100, 0, charset);
    }

    /**
     * Create a new {@link RowIndex}.
     * @param cacheInterval the subtotal cache interval
     * @param prefRows the pref row size
     * @param charset the charset to be used in creating the {@link RowIndex}
     * @return a new {@link RowIndex}
     */
    static RowIndex of(int cacheInterval, int prefRows, Charset charset) {
        return new RowIndex(cacheInterval, prefRows, charset);
    }

    /**
     * Adds the processed rows obtained from a byte array to the index and returns the second array
     * from the processed result.
     * @param bytes the byte array to be processed into rows, where each line is separated
     *              based on the encoding (e.g., CR, LF, or CRLF)
     * @return an array of integers representing the second subarray of the processed byte array,
     *         typically containing counts related to line break characters
     */
    int[] add(byte[] bytes) {
        int[][] rets = rows(bytes);
        add(rets[0]);
        return rets[1];
    }

    /**
     * Adds the specified length array to the index.
     * @param rows the specified length array
     */
    void add(int[] rows) {
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
     * Build subtotal cache.
     */
    public void buildStCache() {
        long st = 0;
        cacheLength = 0;
        stCache = new long[1 + (length / cacheInterval)];
        for (int i = 0; i < length; i ++) {
            if (i % cacheInterval == 0) {
                stCache[cacheLength++] = st;
            }
            st += rowLengths[i];
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
            if (i % cacheInterval == 0 && cacheIndex >= cacheLength) {
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

        int[] rows = rows(bytes)[0];
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
     * Get the row length.
     * @param row the specified row
     * @return the row length
     */
    public int rowLength(int row) {
        return (row >= length) ? 0 : rowLengths[row];
    }

    /**
     * Get the serial position.
     * Length of BOM is excluded.
     * @param row the specified row
     * @param col the specified position in a row
     * @return the serial position
     */
    public long offset(int row, int col) {
        return get(row) + Math.min(rowLengths[row], col);
    }

    /**
     * Get the row floor offset position.
     * @param offset the base offset
     * @return the row floor offset position
     */
    public long rowFloorOffset(long offset) {
        int result = Arrays.binarySearch(stCache, 0, cacheLength, offset);
        if (result >= 0) {
            return stCache[result];
        } else {
            int point = Math.max(~result - 1, 0);
            long st = stCache[point];
            for (int i = point * cacheInterval; i < length; i++) {
                int len = rowLengths[i];
                if (st + len > offset) return st;
                st += len;
            }
            return st;
        }
    }

    /**
     * Get the row ceil offset position.
     * @param offset the base offset
     * @return the row floor offset position
     */
    public long rowCeilOffset(long offset) {
        if (offset <= 0) return 0;
        int result = Arrays.binarySearch(stCache, 0, cacheLength, offset);
        if (result >= 0) {
            return stCache[result];
        } else {
            int point = Math.max(~result - 1, 0);
            long st = stCache[point];
            for (int i = point * cacheInterval; i < length; i++) {
                int len = rowLengths[i];
                st += len;
                if (st >= offset) return st;
            }
            return st;
        }
    }

    /**
     * Get the row-col position.
     * @param offset the offset position
     * @return the row-col position
     */
    public int[] pos(long offset) {
        int result = Arrays.binarySearch(stCache, 0, cacheLength, offset);
        if (result >= 0) {
            return new int[] { result * cacheInterval, 0 };
        } else {
            int point = Math.max(~result - 1, 0);
            long st = stCache[point];
            for (int i = point * cacheInterval; i < length; i++) {
                int len = rowLengths[i];
                if (st + len > offset) {
                    int col = (int) (offset - st);
                    return new int[] { i, col };
                } else {
                    st += len;
                }
            }
            return new int[] { length - 1, rowLengths[length - 1] };
        }
    }

    /**
     * Processes the given byte array to determine the byte lengths of lines and counts of
     * carriage return (CR) and line feed (LF) characters.
     * @param bytes the byte array to be analyzed, where each line is separated
     *              by CR, LF, or CRLF depending on the encoding
     * @return a two-dimensional array of integers, where the first sub-array contains the
     *         byte lengths of each line, and the second sub-array contains the counts of
     *         CR and LF characters
     */
    int[][] rows(byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            return new int[][] { {}, {0, 0} };
        }
        IntArray intArray = IntArray.of();
        int[] crlf = traversRow(bytes, intArray);
        return new int[][] { intArray.get(), crlf };
    }

    int[] traversRow(byte[] bytes, IntArray intArray) {

        int crCount = 0, lfCount = 0, n = 0;

        if (byteUnits == 1) {

            for (byte aByte : bytes) {
                n++;
                if (aByte == '\r') {
                    crCount++;
                } else if (aByte == '\n') {
                    lfCount++;
                    intArray.add(n);
                    n = 0;
                }
            }
            intArray.add(n);

        } else if (byteUnits == 2) {
            // UTF-16 like
            for (int i = 0; i < bytes.length; i += 2) {
                byte aByte1 = bytes[i];
                byte aByte2 = bytes[i + 1];
                n += 2;
                // UTF16 LE  \r\n: 0D 00 0A 00    \n: 0A 00
                // UTF16 BE  \r\n: 00 0D 00 0A    \n: 00 0A
                if ((aByte1 == '\r' && aByte2 == 0) ||
                    (aByte1 == 0 && aByte2 == '\r')) {
                    crCount++;
                } else if ((aByte1 == '\n' && aByte2 == 0) ||
                    (aByte1 == 0 && aByte2 == '\n')) {
                    lfCount++;
                    intArray.add(n);
                    n = 0;
                }
            }
            intArray.add(n);

        } else if (byteUnits == 4) {
            // UTF-32 like
            for (int i = 0; i < bytes.length; i += 4) {
                byte aByte1 = bytes[i];
                byte aByte2 = bytes[i + 1];
                byte aByte3 = bytes[i + 2];
                byte aByte4 = bytes[i + 3];
                n += 4;
                // UTF32 LE  \r\n: 0D 00 00 00 0A 00 00 00    \n: 0A 00 00 00
                // UTF32 BE  \r\n: 00 00 00 0D 00 00 00 0A    \n: 00 00 00 0A
                if ((aByte1 == '\r' && aByte2 == 0 && aByte3 == 0 && aByte4 == 0) ||
                    (aByte1 == 0 && aByte2 == 0 && aByte3 == 0 && aByte4 == '\r')) {
                    crCount++;
                } else if ((aByte1 == '\n' && aByte2 == 0 && aByte3 == 0 && aByte4 == 0) ||
                    (aByte1 == 0 && aByte2 == 0 && aByte3 == 0 && aByte4 == '\n')) {
                    lfCount++;
                    intArray.add(n);
                    n = 0;
                }
            }
            intArray.add(n);

        } else {
            throw new IllegalStateException("Unsupported byte width: " + byteUnits);
        }

        return new int[] { crCount, lfCount };
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
     * Grow this subtotal cache array.
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
     * Gets the subtotal cache array.
     * @return the subtotal cache array
     */
    long[] stCache() {
        return Arrays.copyOf(stCache, cacheLength);
    }

}
