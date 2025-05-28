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
package com.mammb.code.piecetable.core;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@code ChannelBuffer} provides a mechanism to buffer bytes from a {@link SeekableByteChannel}.
 * It implements the {@link Buffer} interface to allow reading bytes, extracting ranges of bytes,
 * and interacting with the underlying channel efficiently.
 * This class is also {@link Closeable}, enabling proper resource management.
 * @author Naotsugu Kobayashi
 */
public class ChannelBuffer implements Buffer, Closeable {

    /** The size of the buffer. */
    static final short PREF_BUF_SIZE = 1024 * 8;

    /** The empty byte array. */
    private static final byte[] EMPTY = {};

    /** The source channel. */
    private final SeekableByteChannel ch;

    /** The current size of the entity to which this channel is connected. */
    private final long length;

    /** The byte buffer. */
    private byte[] buffer;

    /** The offset of the buffer. */
    private long offset;


    /**
     * Create a new {@link ChannelBuffer}.
     * @param ch the source channel
     */
    private ChannelBuffer(SeekableByteChannel ch) {
        try {
            this.length = ch.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.ch = ch;
        this.buffer = EMPTY;
        this.offset = 0;
    }


    /**
     * Create a new {@code ChannelBuffer} from the given {@code Path}.
     * @param path the given {@code Path}
     * @return a new {@code ChannelBuffer}
     */
    public static ChannelBuffer of(Path path) {
        try {
            return new ChannelBuffer(FileChannel.open(path, StandardOpenOption.READ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public byte get(long index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, length));
        }
        if (buffer == EMPTY || index < offset || index >= offset + buffer.length) {
            fillBuffer(index, Math.addExact(index, PREF_BUF_SIZE));
        }
        return buffer[Math.toIntExact(index - offset)];
    }


    @Override
    public byte[] bytes(long from, long to) {

        if (from < 0 || to > length || from > to) {
            throw new IndexOutOfBoundsException(
                "from[%d], to[%d], length[%d]".formatted(from, to, length));
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
        return Arrays.copyOfRange(buffer,
            Math.toIntExact(from - offset), Math.toIntExact(to - offset));
    }


    @Override
    public long length() {
        return length;
    }


    @Override
    public void close() throws IOException {
        ch.close();
    }

    @Override
    public long read(long offset, long length, ByteBuffer buffer) {
        try {
            int read = ch.position(offset).read(buffer);
            if (read == length) return -1;
            if (read > length) {
                buffer.position(buffer.position() - Math.toIntExact(read - length));
                return -1;
            }
            return offset + read;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fill buffer.
     * @param from start position of a channel
     * @param to end position of a channel
     */
    private void fillBuffer(long from, long to) {
        try {
            var bb = ByteBuffer.allocate(
                Math.toIntExact(Math.max(to - from, PREF_BUF_SIZE)));
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
