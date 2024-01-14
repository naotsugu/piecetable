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
package com.mammb.code.piecetable.buffer;

import com.mammb.code.piecetable.array.IntArray;
import java.util.Arrays;

/**
 * UTF-8 bytes array buffer.
 * @author Naotsugu Kobayashi
 */
public class ReadBuffer implements Buffer {

    /** The default size of pitch. */
    private static final short DEFAULT_PITCH = 100;

    /** The elements of buffer. */
    private final byte[] elements;

    /** The length of code point counts. */
    private final int length;

    /** The size of pitch. */
    private final short pilePitch;

    /** The piles. */
    private final int[] piles;

    /** The cache. */
    private final LruCache cache;


    /**
     * Constructor.
     * @param elements the elements of buffer
     * @param length the length of code point counts
     * @param pilePitch the size of pitch
     * @param piles the piles
     */
    private ReadBuffer(byte[] elements, int length, short pilePitch, int[] piles) {
        this.elements = elements;
        this.length = length;
        this.pilePitch = pilePitch;
        this.piles = piles;
        this.cache = LruCache.of();
    }


    /**
     * Create a new buffer.
     * @param elements the elements of buffer
     * @return a new buffer
     */
    public static ReadBuffer of(byte[] elements) {
        return of(elements, DEFAULT_PITCH);
    }


    /**
     * Create a new buffer.
     * @param elements the elements of buffer
     * @param pitch the pitch
     * @return a new buffer
     */
    static ReadBuffer of(byte[] elements, short pitch) {
        int charCount = 0;
        IntArray array = IntArray.of();
        for (int i = 0; i < elements.length; i++) {
            if (charCount++ % pitch == 0) {
                array.add(i);
            }
            i += (Utf8.followsCount(elements[i]) - 1);
        }
        return new ReadBuffer(elements, charCount, pitch, array.get());
    }


    @Override
    public long length() {
        return length;
    }

    @Override
    public byte[] bytes(long rawStart, long rawEnd) {
        return Arrays.copyOfRange(elements, Math.toIntExact(rawStart), Math.toIntExact(rawEnd));
    }

    @Override
    public byte[] bytes() {
        return Arrays.copyOf(elements, elements.length);
    }

    @Override
    public Buffer subBuffer(long start, long end) {
        return of(Arrays.copyOfRange(elements, Math.toIntExact(asIndex(start)), Math.toIntExact(asIndex(end))));
    }

    @Override
    public byte[] charAt(long index) {
        return Utf8.asCharBytes(elements, Math.toIntExact(asIndex(index)));
    }

    @Override
    public long asIndex(long index) {
        if (index == length) {
            return elements.length;
        }
        var cached = cache.get(index);
        if (cached.isPresent()) {
            return cached.get();
        }
        int i = piles[Math.toIntExact(index / pilePitch)];
        int remaining = Math.toIntExact(index % pilePitch);
        for (; remaining > 0 && i < elements.length; remaining--, i++) {
            i += (Utf8.followsCount(elements[i]) - 1);
        }
        cache.put(index, i);
        return i;
    }

    @Override
    public String toString() {
        return new String(bytes(0, elements.length), Utf8.charset());
    }

    String dump() {
        return "elements: %s\npiles: %s"
            .formatted(Arrays.toString(elements), Arrays.toString(piles));
    }

}
