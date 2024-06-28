package com.mammb.dev.picetable.index;

import java.util.Arrays;

public class LineIndex {

    private int[] lineLengths;
    private int length;

    /** sub-total cache. */
    private long[] stCache;
    private int cacheLength;
    private final int cacheInterval;

    public LineIndex(int cacheInterval) {
        lineLengths = new int[0];
        length = 0;

        stCache = new long[0];
        cacheLength = 0;
        this.cacheInterval = cacheInterval;
    }

    public static LineIndex of() {
        return new LineIndex(100);
    }

    static LineIndex of(int cacheInterval) {
        return new LineIndex(cacheInterval);
    }


    void add(byte[] bytes) {

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


    long get(int lineNum) {

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

    void insert(int lineNum, int posAtLine, byte[] bytes) {
        int[] lines = lines(bytes);
        if (lines.length == 0) {
            return;
        }

        if (length + lines.length > lineLengths.length) {
            lineLengths = grow(length + lines.length);
        }

        if (lines.length == 1) {
            lineLengths[lineNum] += lines[0];
        } else {
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
        cacheLength = lineNum / cacheInterval;
    }


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

    int[] lineLengths() {
        return Arrays.copyOf(lineLengths, length);
    }

    long[] stCache() {
        return Arrays.copyOf(stCache, cacheLength);
    }

}
