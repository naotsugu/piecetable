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
package com.mammb.code.piecetable.piece;

import com.mammb.code.piecetable.buffer.Buffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

/**
 * A piece consists of three param.
 * @param target which buffer
 * @param bufIndex start index in the buffer
 * @param length length in the buffer
 * @author Naotsugu Kobayashi
 */
public record Piece(Buffer target, long bufIndex, long length) {

    /** Piece pair. */
    public record Pair(Piece left, Piece right) { }


    /**
     * Split this piece at the specified offset.
     * @param offset the split position in ths piece
     * @return the split pieces
     */
    public Pair split(long offset) {
        if (offset <=  0 || offset >= length) {
            throw new RuntimeException("Illegal offset value. offset[%s], length[%s]"
                    .formatted(offset, length));
        }
        return new Pair(
                new Piece(target, bufIndex, offset),
                new Piece(target, bufIndex + offset, length - offset));
    }


    /**
     * Marge the piece.
     * @param other the piece to be merged
     * @return the merged piece
     */
    public Optional<Piece> marge(Piece other) {
        if (target == other.target) {
            if (end() == other.bufIndex) {
                return Optional.of(new Piece(target, bufIndex, length + other.length));
            }
            if (other.end() == bufIndex) {
                return Optional.of(new Piece(target, other.bufIndex, length + other.length));
            }
        }
        return Optional.empty();
    }


    /**
     * Get the sub buffer of the specified range of this piece.
     * @param start the start index of the range to be copied, inclusive
     * @param end the end index of the range to be copied, exclusive
     * @return the sub buffer of the specified range of this piece
     */
    public Buffer bytes(long start, long end) {
        if (start < 0 || end > end() || start >= end) {
            throw new RuntimeException("Illegal index. start[%s], end[%s]".formatted(start, end));
        }
        return target.subBuffer(bufIndex + start, bufIndex + end);
    }


    /**
     * Get the bytes.
     * @return the bytes
     */
    public Buffer bytes() {
        return bytes(0, length);
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


    /**
     * Get the end position.
     * @return the end position
     */
    public long end() {
        return bufIndex + length;
    }


    @Override
    public String toString() {
        return "Piece[" +
            "target=" + target.getClass().getSimpleName() +
            ", bufIndex=" + bufIndex +
            ", length=" + length +
            ']';
    }

}
