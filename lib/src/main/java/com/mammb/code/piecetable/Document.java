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

import com.mammb.code.piecetable.text.DocumentImpl;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * The document.
 * @author Naotsugu Kobayashi
 */
public interface Document {

    /**
     * Inserts the char sequence into this {@code Document}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the char sequence is to be inserted
     * @param cs the char sequence is to be inserted
     */
    void insert(int row, int col, CharSequence cs);

    /**
     * Inserts the byte array into this {@code Document}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the char sequence is to be inserted
     * @param bytes the byte array is to be inserted
     */
    void insert(int row, int col, byte[] bytes);

    /**
     * Delete the characters from this {@code Document}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the char sequence to be deleted
     * @param len the byte length to be deleted
     */
    void delete(int row, int col, int len);

    /**
     * Gets the byte array at the specified position.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row
     * @param len the byte length
     * @return the byte array
     */
    byte[] get(int row, int col, int len);

    /**
     * Gets the byte array at the specified row.
     * @param row the number of row(zero origin)
     * @return the byte array of the specified row
     */
    byte[] get(int row);

    /**
     * Gets the char sequence at the specified position.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row
     * @param len the byte length
     * @return the char sequence
     */
    CharSequence getText(int row, int col, int len);

    /**
     * Gets the char sequence at the specified row.
     * @param row the number of row(zero origin)
     * @return the char sequence of the specified row
     */
    CharSequence getText(int row);

    /**
     * Get the row size.
     * @return the row size
     */
    int rows();

    /**
     * Get the bytes length of this document holds.
     * @return the bytes length of this document holds
     */
    long length();

    /**
     * Get the charset.
     * @return the charset
     */
    Charset charset();

    /**
     * Get the path.
     * @return the path
     */
    Path path();

    /**
     * Save this document.
     * @param path the path
     */
    void save(Path path);

    /**
     * Create a new {@link Document}.
     * @return a new {@link Document}
     */
    static Document of() {
        return DocumentImpl.of();
    }

    /**
     * Create a new {@link Document}.
     * @param path the path of the file to read
     * @return a new {@link Document}
     */
    static Document of(Path path) {
        return DocumentImpl.of(path);
    }

    /**
     * Create a new {@link Document}.
     * @param path the path of the file to read
     * @param charset the charset
     * @return a new {@link Document}
     */
    static Document of(Path path, Charset charset) {
        return DocumentImpl.of(path, charset);
    }

    /**
     * Create a new {@link Document}.
     * @param path the path of the file to read
     * @param charsetMatches the charset matches
     * @return a new {@link Document}
     */
    static Document of(Path path, CharsetMatch... charsetMatches) {
        return DocumentImpl.of(path, charsetMatches);
    }

}
