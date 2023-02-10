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
import com.mammb.code.piecetable.array.IntArray;
import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * ByteChannel buffer.
 * @author Naotsugu Kobayashi
 */
public class ChannelBuffer implements Buffer, Closeable {

    private static final short PILE_PITCH = 256;

    private final ChannelArray ch;
    private final int length;
    private final short pilePitch;
    private final int[] piles;
    private final LruCache cache;

    private ChannelBuffer(ChannelArray ch, int length, short pilePitch, int[] piles) {
        this.ch = ch;
        this.length = length;
        this.pilePitch = pilePitch;
        this.piles = piles;
        this.cache = LruCache.of();
    }

    public static ChannelBuffer of(SeekableByteChannel channel) {
        return of(channel, PILE_PITCH);
    }

    static ChannelBuffer of(SeekableByteChannel channel, short pitch) {
        var ch = ChannelArray.of(channel);
        var charCount = 0;
        var piles = IntArray.of();
        for (int i = 0; i < ch.length(); i++) {
            if (charCount++ % pitch == 0) {
                piles.add(i);
            }
            i += (Utf8.surrogateCount(ch.get(i)) - 1);
        }
        return new ChannelBuffer(ch, charCount, pitch, piles.get());
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte[] charAt(int index) {
        int rawIndex = asIndex(index);
        return Utf8.asCharBytes(ch.get(rawIndex, Math.min(rawIndex + 4, ch.length())), 0);
    }

    @Override
    public byte[] bytes(int rawStart, int rawEnd) {
        return ch.get(rawStart, rawEnd);
    }

    @Override
    public byte[] bytes() {
        return ch.get(0, ch.length());
    }

    @Override
    public Buffer subBuffer(int start, int end) {
        return ReadBuffer.of(ch.get(asIndex(start), asIndex(end)));
    }

    @Override
    public int asIndex(int index) {
        if (index == length) {
            return ch.length();
        }
        var cached = cache.get(index);
        if (cached.isPresent()) {
            return cached.get();
        }
        int i = piles[index / pilePitch];
        int remaining = index % pilePitch;
        for (; remaining > 0 && i < ch.length(); remaining--, i++) {
            i += (Utf8.surrogateCount(ch.get(i)) - 1);
        }
        cache.put(index, i);
        return i;
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

}
