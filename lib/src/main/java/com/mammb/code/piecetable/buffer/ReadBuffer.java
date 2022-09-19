package com.mammb.code.piecetable.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UTF-8 bytes array buffer.
 * @author Naotsugu Kobayashi
 */
class ReadBuffer implements Buffer {

    private static final short DEFAULT_PITCH = 100;

    private final byte[] elements;
    private final int length;

    private final short pilePitch;
    private final int[] piles;
    private final LruCache cache;

    private ReadBuffer(byte[] elements, int length, short pilePitch, int[] piles) {
        this.elements = elements;
        this.length = length;
        this.pilePitch = pilePitch;
        this.piles = piles;
        this.cache = LruCache.of();
    }

    public static ReadBuffer of(byte[] elements) {
        return of(elements, DEFAULT_PITCH);
    }

    static ReadBuffer of(byte[] elements, short pitch) {
        int charCount = 0;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < elements.length; i++) {
            if (charCount++ % pitch == 0) {
                list.add(i);
            }
            i += (Utf8.surrogateCount(elements[i]) - 1);
        }
        return new ReadBuffer(elements, charCount, pitch, list.stream().mapToInt(i -> i).toArray());
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte[] bytes() {
        return elements;
    }

    @Override
    public Buffer subBuffer(int start, int end) {
        return of(Arrays.copyOfRange(elements, asIndex(start), asIndex(end)));
    }

    @Override
    public byte[] charAt(int index) {
        return Utf8.asCharBytes(elements, asIndex(index));
    }

    @Override
    public int asIndex(int index) {
        if (index == length) {
            return elements.length;
        }
        var cached = cache.get(index);
        if (cached.isPresent()) {
            return cached.get();
        }
        int i = piles[index / pilePitch];
        int remaining = index % pilePitch;
        for (; remaining > 0 && i < elements.length; remaining--, i++) {
            i += (Utf8.surrogateCount(elements[i]) - 1);
        }
        cache.put(index, i);
        return i;
    }

    String dump() {
        return "elements: %s\npiles: %s"
            .formatted(Arrays.toString(elements), Arrays.toString(piles));
    }

    @Override
    public String toString() {
        return new String(bytes(), Utf8.charset());
    }

}
