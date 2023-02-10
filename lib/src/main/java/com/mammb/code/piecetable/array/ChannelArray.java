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

    private static final short PREF_BUF_SIZE = 1024 * 4;
    private static final byte[] EMPTY = {};

    private final SeekableByteChannel ch;
    private final int chSize;

    private byte[] buffer;
    private int offset;

    private ChannelArray(SeekableByteChannel ch) {
        Objects.requireNonNull(ch);
        try {
            this.chSize = Math.toIntExact(ch.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.ch = ch;
        this.buffer = EMPTY;
        this.offset = 0;
    }

    public static ChannelArray of(SeekableByteChannel ch) {
        return new ChannelArray(ch);
    }

    public byte get(int index) {
        if (index < 0 || index >= chSize) {
            throw new IndexOutOfBoundsException(
                "index[%d], length[%d]".formatted(index, chSize));
        }
        if (buffer == EMPTY || index < offset || index >= offset + buffer.length) {
            fillBuffer(index, Math.addExact(index, PREF_BUF_SIZE));
        }
        return buffer[index - offset];
    }

    public byte[] get(int from, int to) {
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
        return Arrays.copyOfRange(buffer, from - offset, to - offset);
    }

    public int length() {
        return chSize;
    }

    public void clear() {
        this.buffer = EMPTY;
        this.offset = 0;
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

    private void fillBuffer(int from, int to) {
        try {
            var bb = ByteBuffer.allocate(
                Math.max(to, Math.addExact(from, PREF_BUF_SIZE) - from));
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
