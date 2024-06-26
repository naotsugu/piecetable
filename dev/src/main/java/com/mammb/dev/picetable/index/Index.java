package com.mammb.dev.picetable.index;

import java.util.Arrays;

public class Index {

    private int[] lines = new int[0];
    private int length = 0;

    void put(byte[] bytes) {
        int lineLen = 0;
        for (int i = 0; i < bytes.length; i++) {
            lineLen++;
            if (bytes[i] == '\n') {
                if (length + 1 > lines.length) {
                    lines = grow();
                }
                lines[length++] = lineLen;
                lineLen = 0;
            }
        }
    }

    long get(int lineNum) {
        // TODO get from cache
        long ret = 0;
        for (int i = 0; i < lines.length && i < lineNum; i++) {
            ret += lines[i];
        }
        return ret;
    }

    void insert(int lineNum, int posAtLine, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\n') {
                lines[lineNum++] = posAtLine;
                // TODO evict cache
                if (length + 1 > lines.length) {
                    lines = grow();
                }
                System.arraycopy(lines, lineNum, lines, lineNum + 1, length - lineNum);
                length++;
                posAtLine = 0;
            } else {
                posAtLine++;
            }
        }
        lines[lineNum] = posAtLine;
    }


    void delete(int lineNum, int posAtLine, int len) {

    }


    private int[] grow() {
        int oldCapacity = lines.length;
        if (length == 0 || oldCapacity == 0) {
            return lines = new int[1024];
        } else {
            int newLen = lines.length + 1024;
            if (newLen > Integer.MAX_VALUE - 8) {
                throw new OutOfMemoryError(
                    "Required array length %d %d is too large".formatted(lines.length, newLen));
            }
            return lines = Arrays.copyOf(lines, newLen);
        }
    }

}
