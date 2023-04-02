/*
 * Copyright 2019-2023 the original author or authors.
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

import com.mammb.code.piecetable.array.ByteArray;
import com.mammb.code.piecetable.array.IntArray;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Appendable UTF-8 bytes array buffer.
 * @author Naotsugu Kobayashi
 */
public class GrowBuffer implements AppendBuffer {

    /** The default size of pitch. */
    private static final short DEFAULT_PITCH = 100;

    /** The elements of buffer. */
    private final ByteArray elements;

    /** The length of code point counts. */
    private int length;

    /** The size of pitch. */
    private final short pilePitch;

    /** The piles. */
    private final IntArray piles;

    /** The cache. */
    private final LruCache cache;


    /**
     * Constructor.
     * @param elements the elements of buffer
     * @param length the length of code point counts
     * @param pilePitch the size of pitch
     * @param piles the piles
     */
    private GrowBuffer(ByteArray elements, int length, short pilePitch, IntArray piles) {
        this.elements = elements;
        this.length = length;
        this.pilePitch = pilePitch;
        this.piles = piles;
        this.cache = LruCache.of();
    }


    /**
     * Create a new buffer.
     * @return a new buffer
     */
    public static AppendBuffer of() {
        return new GrowBuffer(ByteArray.of(), 0, DEFAULT_PITCH, IntArray.of());
    }


    /**
     * Create a new buffer.
     * @param pitch the pitch
     * @return a new buffer
     */
    static AppendBuffer of(short pitch) {
        return new GrowBuffer(ByteArray.of(), 0, pitch, IntArray.of());
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
            byte[] bytes = buffer.charAt(i);
            elements.add(buffer.charAt(i));
            if (length++ % pilePitch == 0) {
                piles.add(elements.length() - bytes.length);
            }
        }
    }


    @Override
    public void append(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            elements.add(b);
            if (!Utf8.isLower(b)) {
                if (length++ % pilePitch == 0) {
                    piles.add(elements.length() - 1);
                }
            }
        }
    }


    @Override
    public void clear() {
        elements.clear();
        length = 0;
        piles.clear();
        cache.clear();
    }

    @Override
    public Buffer subBuffer(int start, int end) {
        return ReadBuffer.of(Arrays.copyOfRange(elements.get(), asIndex(start), asIndex(end)));
    }

    @Override
    public byte[] bytes(int rawStart, int rawEnd) {
        return elements.get(rawStart, rawEnd);
    }

    @Override
    public byte[] bytes() {
        return elements.get();
    }

    @Override
    public byte[] charAt(int index) {
        return Utf8.asCharBytes(elements.get(), asIndex(index));
    }

    @Override
    public int asIndex(int index) {
        if (index == length) {
            return elements.length();
        }
        var cached = cache.get(index);
        if (cached.isPresent()) {
            return cached.get();
        }
        int i = piles.get(index / pilePitch);
        int remaining = index % pilePitch;
        for (; remaining > 0 && i < elements.length(); remaining--, i++) {
            i += (Utf8.followsCount(elements.get(i)) - 1);
        }
        cache.put(index, i);
        return i;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String toString() {
        return new String(bytes(0, elements.length()), StandardCharsets.UTF_8);
    }

    String dump() {
        return "elements: %s\npiles: %s".formatted(elements, piles);
    }

}
