package com.mammb.code.piecetable.buffer;

import com.mammb.code.piecetable.array.ByteArray;
import com.mammb.code.piecetable.array.IntArray;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class AppendBuffer implements AppendableBuffer {

    private static final short DEFAULT_GAP = 200;

    private final short gap;
    private final ByteArray values;
    private final IntArray index;
    private int length;

    private AppendBuffer(ByteArray values, int length, short gap, IntArray index) {
        this.gap = gap;
        this.values = values;
        this.index = index;
        this.length = length;
    }

    public static AppendBuffer of() {
        return new AppendBuffer(ByteArray.of(), 0, DEFAULT_GAP, IntArray.of());
    }

    static AppendBuffer of(short gap) {
        return new AppendBuffer(ByteArray.of(), 0, gap, IntArray.of());
    }

    @Override
    public void append(CharSequence cs) {
        if (cs instanceof String s) {
            append(s.getBytes(StandardCharsets.UTF_8));
        } else {
            append(cs.toString());
        }
    }

    @Override
    public void append(Buffer buffer) {
        for (int i = 0; i < buffer.length(); i++) {
            values.add(buffer.charAt(i));
            if (length % gap == 0) {
                index.add(length);
            }
            length++;
        }
    }

    @Override
    public void append(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            values.add(b);
            if (!Utf8.isSurrogateRetain(b)) {
                if (length % gap == 0) {
                    index.add(length);
                }
                length++;
            }
        }
    }

    @Override
    public Buffer subBuffer(int start, int end) {
        return ReadBuffer.of(Arrays.copyOfRange(values.get(), asIndex(start), asIndex(end)));
    }

    @Override
    public byte[] bytes() {
        return values.get();
    }

    @Override
    public byte[] charAt(int charIndex) {
        return Utf8.asCharBytes(values.get(), asIndex(charIndex));
    }

    private int asIndex(int charIndex) {
        int i = index.get(charIndex / gap);
        for (int remaining = charIndex % gap; remaining > 0 && i < values.length(); remaining--, i++) {
            i += (Utf8.surrogateCount(values.get(i)) - 1);
        }
        return i;
    }

    @Override
    public int length() {
        return length;
    }

    String dump() {
        return "values: " + values + "\nsegmentIndexes:" + index;
    }

    @Override
    public String toString() {
        return new String(bytes(), StandardCharsets.UTF_8);
    }

}
