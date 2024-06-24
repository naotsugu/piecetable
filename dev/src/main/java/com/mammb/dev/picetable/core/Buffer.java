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
package com.mammb.dev.picetable.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * The buffer
 * @author Naotsugu Kobayashi
 */
public interface Buffer {

    /**
     * Gets the byte at the specified index.
     * @param index the index of the buffers bytes
     * @return the byte at the specified index of this buffer
     */
    byte get(long index);

    /**
     * Get the byte array at the specified index.
     * @param startIndex the start raw index of the byte value
     * @param endIndex the end raw index of the byte value
     * @return the byte array at the specified index
     */
    byte[] bytes(long startIndex, long endIndex);

    /**
     * Get the length of buffer.
     * @return the count of bytes
     */
    long length();

    /**
     * Get whether this buffer is empty.
     * @return {@code true}, if this buffer is empty
     */
    default boolean isEmpty() {
        return this.length() == 0;
    }


    /**
     * Write this buffer values to the specified channel.
     * @param channel the byte channel
     * @param buf the buffer used for writing
     * @param offset the code point based offset
     * @param length the code point based length
     * @return the written Length
     * @throws IOException if some other I/O error occurs
     */
    default long write(WritableByteChannel channel, ByteBuffer buf,
            long offset, long length) throws IOException {

        long from = offset;
        long to   = offset + length;

        for (long i = from; i < to;) {
            long m = Math.min(i + buf.remaining(), to);
            buf.put(bytes(i, m));
            buf.flip();
            int n = channel.write(buf);
            i += n;
            buf.compact();
            if (n <= 0) break;
        }
        return to - from;
    }


    /**
     * Create a new in-memory buffer.
     * @return a new in-memory buffer
     */
    static Buffer of(byte[] bytes) {
        return new Buffer() {
            /** The elements of buffer. */
            private final byte[] elements = bytes;

            @Override
            public byte get(long index) {
                return elements[Math.toIntExact(index)];
            }

            @Override
            public byte[] bytes(long startIndex, long endIndex) {
                return Arrays.copyOfRange(elements,
                    Math.toIntExact(startIndex), Math.toIntExact(endIndex));
            }

            @Override
            public long length() {
                return elements.length;
            }
        };
    }

}
