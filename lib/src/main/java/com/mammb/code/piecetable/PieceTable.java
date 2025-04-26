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
package com.mammb.code.piecetable;

import com.mammb.code.piecetable.core.PieceTableImpl;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * PieceTable.
 * PieceTable is a data structure typically used to represent a text document
 * while it is edited in a text editor.
 * <p>
 * Initially a reference to the whole of the original file is created, which represents the as yet unchanged file.
 * Subsequent inserts and deletes replace a span by combinations of one, two, or three references to sections of
 * either the original document or to a buffer holding inserted text.
 * </p>
 *
 * <pre>
 *            PieceList pieces
 *            -------------------------------------
 *            | Buffer target | bufIndex | length |
 *            -------------------------------------
 *  (1) Piece |  readBuffer  |       0  |      6  |
 *            -------------------------------------
 *  (2) Piece | appendBuffer |       0  |      3  |
 *            -------------------------------------
 *  (3) Piece |  readBuffer  |       7  |      2  |
 *            -------------------------------------
 *            ...
 *
 *
 *  readBuffer(read only)                  appendBuffer
 *  ---------------------------------      -------------
 *  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |      | 0 | 1 | 2 | ...
 *  | L | o | r | e | m |   | u | m |      | i | p | s | ...
 *  ---------------------------------      -------------
 *  \__________(1)_________/
 *                                         \____(2)___/
 *                          \_(3)__/
 *
 *  (1)                     (2)          (3)
 *  | L | o | r | e | m |   | i | p | s | u | m |
 * </pre>
 *
 * @author Naotsugu Kobayashi
 */
public interface PieceTable {

    /**
     * Inserts the bytes into this {@code PieceTable}.
     * @param pos the offset
     * @param bytes the bytes to be inserted
     */
    void insert(long pos, byte[] bytes);

    /**
     * Removes the bytes in a substring of this {@code PieceTable}.
     * @param pos the beginning index, inclusive
     * @param len the length to be deleted
     */
    void delete(long pos, int len);

    /**
     * Get the byte array of the specified range of this piece table.
     * @param pos the start index of the range to be copied, inclusive
     * @param len the length of the range to be copied
     * @return the byte array of the specified range of this piece table
     */
    byte[] get(long pos, int len);

    /**
     * Get the length of bytes this piece table holds.
     * @return the length of bytes
     */
    long length();

    /**
     * Save this piece table.
     * @param path the path
     */
    void save(Path path);

    /**
     * Reads the contents into the specified byte buffer callback.
     * Provides access to content in an efficient manner via byte buffers.
     * {@snippet :
     * pieceTable.read(0, -1, byteBuffer -> {
     *     byteBuffer.flip();
     *     var b = byteBuffer.get();
     *     // ...
     *     byteBuffer.compact();
     *     return true;
     * });
     * }
     * @param offset the offset
     * @param limitLength the limit length({@code -1} are no limit)
     * @param traverseCallback the specified byte buffer callback
     */
    void read(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback);

    /**
     * Reads the contents into the specified byte buffer.
     * @param offset the offset
     * @param length the length
     * @param bb byte buffer
     * @return the read length
     */
    long read(long offset, long length, ByteBuffer bb);

    /**
     * Get the default implementation of the piece table.
     * @return the piece table
     */
    static PieceTable of() {
        return PieceTableImpl.of();
    }

    /**
     * Get the default implementation of the piece table.
     * @param path the path of the read file
     * @return the piece table
     */
    static PieceTable of(Path path) {
        return PieceTableImpl.of(path);
    }

}
