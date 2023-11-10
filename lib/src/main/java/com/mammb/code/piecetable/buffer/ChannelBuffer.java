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

import com.mammb.code.piecetable.array.ChannelArray;
import com.mammb.code.piecetable.array.LongArray;
import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.function.Consumer;

/**
 * ByteChannel buffer.
 * @author Naotsugu Kobayashi
 */
public class ChannelBuffer implements Buffer, Closeable {

    /** The default size of pitch. */
    private static final short DEFAULT_PITCH = 512;

    /** The channel array. */
    private final ChannelArray ch;

    /** The length of code point counts. */
    private final int length;

    /** The size of pitch. */
    private final short pilePitch;

    /** The piles. */
    private final long[] piles;

    /** The lru cache. */
    private final LruCache cache;


    /**
     * Constructor.
     * @param ch the ChannelArray
     * @param length the length of code point counts
     * @param pilePitch the size of pitch
     * @param piles the piles
     */
    private ChannelBuffer(ChannelArray ch, int length, short pilePitch, long[] piles) {
        this.ch = ch;
        this.length = length;
        this.pilePitch = pilePitch;
        this.piles = piles;
        this.cache = LruCache.of();
    }


    /**
     * Create a new Buffer.
     * @param channel the byte channel
     * @return a created buffer
     */
    public static Buffer of(SeekableByteChannel channel) {
        return of(channel, DEFAULT_PITCH, null);
    }


    /**
     * Create a new Buffer.
     * @param channel the byte channel
     * @param pitch the pitch
     * @return a created buffer
     */
    public static Buffer of(SeekableByteChannel channel, short pitch) {
        return of(channel, pitch, null);
    }


    /**
     * Create a new Buffer.
     * @param channel the byte channel
     * @param traverse the bytes traverse at initial loading
     * @return a created buffer
     */
    public static Buffer of(SeekableByteChannel channel, Consumer<byte[]> traverse) {
        return of(channel, DEFAULT_PITCH, traverse);
    }


    /**
     * Create a new Buffer.
     * @param channel the byte channel
     * @param pitch the pitch
     * @param consumer the bytes consumer at initial loading
     * @return a created buffer
     */
    static Buffer of(SeekableByteChannel channel, short pitch, Consumer<byte[]> consumer) {
        var ch = ChannelArray.of(channel);
        var charCount = 0;
        var piles = LongArray.of(Math.toIntExact(ch.length() / pitch));
        for (int i = 0; i < ch.length(); i++) {
            if (charCount++ % pitch == 0) {
                piles.add(i);
            }
            final byte b = ch.get(i);
            final short followsCount = Utf8.followsCount(b);
            if (consumer != null) {
                if (followsCount == 1) {
                    consumer.accept(new byte[] { b });
                } else {
                    consumer.accept(ch.get(i, i + followsCount));
                }
            }
            i += (followsCount - 1);
        }
        return new ChannelBuffer(ch, charCount, pitch, piles.get());
    }


    @Override
    public long length() {
        return length;
    }


    @Override
    public byte[] charAt(long index) {
        long rawIndex = asIndex(index);
        return Utf8.asCharBytes(ch.get(rawIndex, Math.min(rawIndex + 4, ch.length())), 0);
    }


    @Override
    public byte[] bytes(long rawStart, long rawEnd) {
        return ch.get(rawStart, rawEnd);
    }


    @Override
    public byte[] bytes() {
        return ch.get(0, ch.length());
    }


    @Override
    public Buffer subBuffer(long start, long end) {
        return ReadBuffer.of(ch.get(asIndex(start), asIndex(end)));
    }


    @Override
    public long asIndex(long index) {
        if (index == length) {
            return ch.length();
        }
        var cached = cache.get(index);
        if (cached.isPresent()) {
            return cached.get();
        }
        long i = piles[Math.toIntExact(index / pilePitch)];
        int remaining = Math.toIntExact(index % pilePitch);
        for (; remaining > 0 && i < ch.length(); remaining--, i++) {
            i += (Utf8.followsCount(ch.get(i)) - 1);
        }
        cache.put(index, i);
        return i;
    }


    @Override
    public void close() throws IOException {
        ch.close();
    }

}
