package com.mammb.code.piecetable.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ReadBuffer implements Buffer {

    private static final short DEFAULT_GAP = 200;

    private final short gap;
    private final byte[] values;
    private final int[] index;
    private final int length;

    private ReadBuffer(byte[] values, int length, short gap, int[] index) {
        this.values = values;
        this.length = length;
        this.gap = gap;
        this.index = index;
    }

    public static ReadBuffer of(byte[] values) {
        return of(values, DEFAULT_GAP);
    }

    static ReadBuffer of(byte[] values, short gap) {
        int charCount = 0;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (charCount++ % gap == 0) {
                list.add(i);
            }
            i += (Utf8.surrogateCount(values[i]) - 1);
        }
        return new ReadBuffer(values, charCount, gap, list.stream().mapToInt(i -> i).toArray());
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte[] bytes() {
        return values;
    }

    @Override
    public Buffer subBuffer(int start, int end) {
        return of(Arrays.copyOfRange(values, asIndex(start), asIndex(end)));
    }

    @Override
    public byte[] charAt(int charIndex) {
        return Utf8.asCharBytes(values, asIndex(charIndex));
    }

    private int asIndex(int charIndex) {
        int i = index[charIndex / gap];
        for (int remaining = charIndex % gap; remaining > 0 && i < values.length; remaining--, i++) {
            i += (Utf8.surrogateCount(values[i]) - 1);
        }
        return i;
    }

    String dump() {
        return "values: " + Arrays.toString(values) + "\nsegmentIndexes:" + Arrays.toString(index);
    }

    @Override
    public String toString() {
        return new String(bytes(), Utf8.charset());
    }

}
