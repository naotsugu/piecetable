/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.search;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Interface representing a source of searchable content. This source provides
 * methods to retrieve content information and manipulate content based on its
 * byte representation rather than its in-memory Java representation.
 * @author Naotsugu Kobayashi
 */
public interface SearchSource {

    /**
     * Get the charset.
     * @return the charset
     */
    Charset charset();

    /**
     * Get the bytes length of this document holds.
     * Not the javas UTF-16 encoded memory size.
     * @return the bytes length of this document holds
     */
    long length();

    /**
     * Get the serial position.
     * Represents the byte position from the beginning of the file.
     * This position does not include bom.
     * @param row the specified row
     * @param col the specified position in a row
     * @return the serial position
     */
    long offset(int row, int col);

    /**
     * Get the row-col position.
     * @param offset the serial position
     * @return the row-col position
     */
    int[] pos(long offset);

    /**
     * Get the row floor serial position.
     * @param offset the base serial
     * @return the row floor serial position
     */
    long rowFloorOffset(long offset);

    /**
     * Get the row ceil serial position.
     * @param offset the base serial
     * @return the row floor serial position
     */
    long rowCeilOffset(long offset);

    /**
     * Reads the contents into the specified byte buffer.
     * @param offset the offset
     * @param length the length
     * @param bb byte buffer
     * @return the read length
     */
    long bufferRead(long offset, long length, ByteBuffer bb);

    /**
     * Reads the contents into the specified byte buffer callback.
     * @param offset the offset
     * @param limitLength the limit length({@code -1} are no limit)
     * @param traverseCallback the specified byte buffer callback
     */
    void bufferRead(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback);

}
