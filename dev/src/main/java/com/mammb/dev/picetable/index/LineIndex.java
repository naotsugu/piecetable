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
package com.mammb.dev.picetable.index;

import java.util.Arrays;

/**
 * The LineIndex.
 * Holds the byte length of each line as an index.
 * @author Naotsugu Kobayashi
 */
public class LineIndex {

    /** The line lengths. */
    private int[] lineLengths;
    /** The length of line lengths array. */
    private int length;

    /** The sub-total cache. */
    private long[] stCache;
    /** The sub-total cache length. */
    private int cacheLength;
    /** The sub-total cache interval. */
    private final int cacheInterval;


    /**
     * Create a new {@code LineIndex}.
     * @param cacheInterval the sub-total cache interval
     */
    private LineIndex(int cacheInterval) {
        lineLengths = new int[0];
        length = 0;

        stCache = new long[0];
        cacheLength = 0;
        this.cacheInterval = cacheInterval;
    }


    /**
     * Create a new {@code LineIndex}.
     * @return a new {@code LineIndex}
     */
    public static LineIndex of() {
        return new LineIndex(100);
    }


    /**
     * Create a new {@code LineIndex}.
     * @param cacheInterval the sub-total cache interval
     * @return a new {@code LineIndex}
     */
    static LineIndex of(int cacheInterval) {
        return new LineIndex(cacheInterval);
    }


    /**
     * Adds the specified byte array to the index.
     * @param bytes the specified byte array to be added
     */
    public void add(byte[] bytes) {

        int[] lines = lines(bytes);
        if (lines.length == 0) {
            return;
        }

        if (length + lines.length > lineLengths.length) {
            lineLengths = grow(length + lines.length);
        }

        if (length == 0) {
            length++;
        }
        for (int i = 0; i < lines.length; i++) {
            lineLengths[length - 1] += lines[i];
            if (lines.length > 1 && i < lines.length - 1) {
                // lines |0|
                // lines |0|length++|1|
                // lines |0|length++|1|length++|2|
                // lines |0|length++|1|length++|2|length++|3|
                length++;
            }
        }

    }


    /**
     * Gets the byte length of the specified line.
     * @param lineNum the specified line
     * @return the byte length of the specified line
     */
    public long get(int lineNum) {

        int startLine = 0;
        long startPos = 0;

        int cacheIndex = lineNum / cacheInterval;
        if (cacheIndex > 0 && cacheIndex < cacheLength) {
            startLine = cacheIndex * cacheInterval;
            startPos = stCache[cacheIndex];
        }

        for (int i = startLine; i < length && i < lineNum; i++) {
            if (i % cacheInterval == 0) {
                if (cacheLength + 1 > stCache.length) {
                    stCache = growCache(cacheLength + 1);
                }
                int chIndex = i / cacheInterval;
                stCache[chIndex] = startPos;
                cacheLength = chIndex + 1;
            }
            startPos += lineLengths[i];
        }
        return startPos;
    }

    /**
     * Insert the specified byte array to the index.
     * @param lineNum the specified line
     * @param posAtLine the specified position in a line
     * @param bytes the specified byte array to be inserted
     */
    public void insert(int lineNum, int posAtLine, byte[] bytes) {
        int[] lines = lines(bytes);
        if (lines.length == 0) {
            return;
        }

        if (length + lines.length > lineLengths.length) {
            lineLengths = grow(length + lines.length);
        }
        cacheLength = lineNum / cacheInterval;

        if (lines.length == 1) {
            // insert operation within a single line
            lineLengths[lineNum] += lines[0];
        } else {
            // insert operation across multiple lines
            System.arraycopy(lineLengths, lineNum + 1,
                lineLengths, lineNum + lines.length,
                length - (lineNum + 1));
            int tail = lineLengths[lineNum] - posAtLine;
            lineLengths[lineNum++] = posAtLine + lines[0];
            for (int i = 1; i < lines.length - 1; i++) {
                lineLengths[lineNum++] = lines[i];
            }
            lineLengths[lineNum] = tail + lines[lines.length - 1];
        }
        length += lines.length - 1;
    }


    /**
     * Delete the specified byte length of the index.
     * @param lineNum the specified line
     * @param posAtLine the specified position in a line
     * @param len the specified byte length to be deleted
     */
    public void delete(int lineNum, int posAtLine, int len) {

        if (len <= 0) {
            return;
        }

        cacheLength = lineNum / cacheInterval;

        if ((lineLengths[lineNum] - posAtLine) > len) {

            // delete operation within a single line
            // |a|b|c|d|$|       ->        |a|d|$|
            //   ^---  posAtLine:1, len:2
            lineLengths[lineNum] -= len;

        } else {

            // delete operation across multiple lines
            // 0 |a|b|$|    delete(          ->   |a|f|$|
            // 1 |c|d|$|      lineNum = 0,        |g|h|$|
            // 2 |e|f|$|      posAtLine = 1,
            // 3 |g|h|$|      len = 6)

            len -= lineLengths[lineNum] - posAtLine;
            lineLengths[lineNum] = posAtLine;
            int lines = 0;
            do {
                len -= lineLengths[lineNum + ++lines];
            } while (len > 0);
            lineLengths[lineNum] += (-len); // merge the rest to the first line
            System.arraycopy(
                lineLengths, lineNum + 1 + lines,
                lineLengths, lineNum + 1,
                length - (lineNum + 1 + lines));
            length -= lines;
        }
    }


    /**
     * Converts the specified byte array to line-by-line byte length.
     * @param bytes the specified byte array
     * @return line-by-line byte length
     */
    static int[] lines(byte[] bytes) {

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
        int oldCapacity = lineLengths.length;
        if (oldCapacity > 0) {
            int newCapacity = Math.min(
                Math.max(minCapacity, oldCapacity >> 1),
                Integer.MAX_VALUE - 8);
            return lineLengths = Arrays.copyOf(lineLengths, newCapacity);
        } else {
            return lineLengths = new int[Math.max(100, minCapacity)];
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
     * Gets the line lengths array.
     * @return the line lengths array
     */
    int[] lineLengths() {
        return Arrays.copyOf(lineLengths, length);
    }

    /**
     * Gets the sub-total cache array.
     * @return the sub-total cache array
     */
    long[] stCache() {
        return Arrays.copyOf(stCache, cacheLength);
    }

}
