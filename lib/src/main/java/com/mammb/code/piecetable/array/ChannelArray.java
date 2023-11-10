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
package com.mammb.code.piecetable.array;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.Objects;

/**
 * Buffered byte channel.
 * @author Naotsugu Kobayashi
 */
public class ChannelArray implements Closeable {

    /** The size of buffer. */
    private static final short PREF_BUF_SIZE = 1024 * 4;

    /** The empty byte array. */
    private static final byte[] EMPTY = {};

    /** The source channel. */
    private final SeekableByteChannel ch;

    /** The current size of entity to which this channel is connected. */
    private final long chSize;

    /** The byte buffer. */
    private byte[] buffer;

    /** The offset of buffer. */
    private long offset;


    /**
     * Create a new {@link ChannelArray}.
     * @param ch the source channel
     */
    private ChannelArray(SeekableByteChannel ch) {
        Objects.requireNonNull(ch);
        try {
            this.chSize = ch.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.ch = ch;
        this.buffer = EMPTY;
        this.offset = 0;
    }


    /**
     * Create a new {@code ChannelArray} from the given {@code SeekableByteChannel}.
     * @param ch the given {@code SeekableByteChannel}
     * @return a new {@code ChannelArray}
     */
    public static ChannelArray of(SeekableByteChannel ch) {
        return new ChannelArray(ch);
    }


    /**
     * Get byte at the specified index position.
     * @param index the specified index position
     * @return byte value
     */
    public byte get(long index) {
        if (index < 0 || index >= chSize) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, chSize));
        }
        if (buffer == EMPTY || index < offset || index >= offset + buffer.length) {
            fillBuffer(index, Math.addExact(index, PREF_BUF_SIZE));
        }
        return buffer[Math.toIntExact(index - offset)];
    }


    /**
     * Get copies the specified range of this array.
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive
     * @return a new array containing the specified range from the original array
     */
    public byte[] get(long from, long to) {
        if (from < 0 || to > chSize || from > to) {
            throw new IndexOutOfBoundsException(
                "from[%d], to[%d], length[%d]".formatted(from, to, chSize));
        }

        if (buffer == EMPTY || from < offset || to > offset + buffer.length) {
            fillBuffer(from, to);
            if (buffer.length > PREF_BUF_SIZE << 8) {
                // if too large, trim buffer
                byte[] ret = buffer;
                buffer = Arrays.copyOf(buffer, PREF_BUF_SIZE);
                return ret;
            }
        }
        return Arrays.copyOfRange(buffer, Math.toIntExact(from - offset), Math.toIntExact(to - offset));
    }


    /**
     * Clear this array.
     */
    public void clear() {
        this.buffer = EMPTY;
        this.offset = 0;
    }


    /**
     * Get the length of byte array.
     * @return the length of byte array
     */
    public long length() {
        return chSize;
    }


    @Override
    public void close() throws IOException {
        ch.close();
    }


    /**
     * Fill buffer.
     * @param from start position of channel
     * @param to end position of channel
     */
    private void fillBuffer(long from, long to) {
        try {
            var bb = ByteBuffer.allocate(Math.toIntExact(
                Math.max(to, Math.addExact(from, PREF_BUF_SIZE)) - from));
            ch.position(from);
            ch.read(bb);
            bb.flip();
            buffer = Arrays.copyOf(bb.array(), bb.limit());
            offset = from;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
