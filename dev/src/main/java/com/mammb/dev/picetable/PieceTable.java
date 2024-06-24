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
package com.mammb.dev.picetable;

/**
 * PieceTable.
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
     * Get the length of bytes this piece table holds.
     * @return the length of bytes
     */
    long length();

}
