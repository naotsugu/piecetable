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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A piece consists of three param.
 * @param target which buffer
 * @param bufIndex start index in the buffer
 * @param length length in the buffer
 * @author Naotsugu Kobayashi
 */
public record Piece(Buffer target, long bufIndex, long length) {

    /**
     * Get the end position.
     * @return the end position
     */
    public long end() {
        return bufIndex + length;
    }


    /**
     * Split this piece at the specified offset.
     * @param offset the split position in ths piece
     * @return the split pieces
     */
    public Piece[] split(long offset) {
        if (offset <  0 || offset > length) {
            throw new RuntimeException("Illegal offset value. offset[%s], length[%s]"
                .formatted(offset, length));
        }
        if (offset == 0 || offset == length) {
            return new Piece[] { this };
        } else {
            return new Piece[]{
                new Piece(target, bufIndex, offset),
                new Piece(target, bufIndex + offset, length - offset)
            };
        }
    }


    /**
     * Get the byte array of the specified range of this piece.
     * @param offset the start index of the range to be copied, inclusive
     * @param len the length of the range to be copied
     * @return the byte array of the specified range of this piece
     */
    public byte[] bytes(long offset, int len) {
        if (offset < 0 || offset + len > length) {
            throw new RuntimeException("Illegal index. offset[%s], len[%s]".formatted(offset, len));
        }
        return target.bytes(bufIndex + offset, bufIndex + offset + len);
    }


    /**
     * Get the bytes.
     * @return the bytes
     */
    public byte[] bytes() {
        return target.bytes(bufIndex, bufIndex + length);
    }


    /**
     * Writes a sequence of bytes to the channel from this piece.
     * @param channel the channel to write to
     * @param buf the buffer used for writing
     * @return the number of bytes written, possibly zero
     * @throws IOException If some other I/O error occurs
     */
    long writeTo(WritableByteChannel channel, ByteBuffer buf) throws IOException {
        return target.write(channel, buf, bufIndex, length);
    }

}
