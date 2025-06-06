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
package com.mammb.code.piecetable;

import com.mammb.code.piecetable.text.DocumentImpl;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.function.Consumer;

/**
 * The document.
 * Provides an abstraction for the document to be edited.
 * Represents the contract for a {@code Document} that provides various functionalities
 * needed for handling, editing, and managing textual and binary content using rows and columns.
 * @author Naotsugu Kobayashi
 * */
public interface Document {

    /**
     * Inserts the char sequence into this {@code Document}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the char sequence is to be inserted
     * @param cs the char sequence is to be inserted
     */
    void insert(int row, int col, CharSequence cs);

    /**
     * Delete the characters from this {@code Document}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the char sequence to be deleted
     * @param cs the char sequence is to be deleted
     */
    void delete(int row, int col, CharSequence cs);

    /**
     * Gets the char sequence at the specified row.
     * @param row the number of row(zero origin)
     * @return the char sequence of the specified row
     */
    CharSequence getText(int row);

    /**
     * Usually use {@link Document#insert(int, int, CharSequence)}.
     * Inserts the byte array into this {@code Document}.
     * @param row the number of row(zero origin)
     * @param rawCol the byte position on the row where the char sequence is to be inserted (The value must be encoded in the appropriate Charset)
     * @param bytes the byte array is to be inserted (The value must be encoded in the appropriate Charset)
     */
    void insert(int row, int rawCol, byte[] bytes);

    /**
     * Usually use {@link Document#delete(int, int, CharSequence)}.
     * Delete the characters from this {@code Document}.
     * @param row the number of row(zero origin)
     * @param rawCol the byte position on the row where the char sequence to be deleted (The value must be encoded in the appropriate Charset)
     * @param rawLen the byte length to be deleted (The value must be encoded in the appropriate Charset)
     */
    void delete(int row, int rawCol, int rawLen);

    /**
     * Usually use {@link Document#get(int)}.
     * Gets the byte array at the specified position.
     * @param row the number of row(zero origin)
     * @param rawCol the byte position on the row (The value must be encoded in the appropriate Charset)
     * @param rawLen the byte length (The value must be encoded in the appropriate Charset)
     * @return the byte array
     */
    byte[] get(int row, int rawCol, int rawLen);

    /**
     * Gets the byte array at the specified row.
     * @param row the number of row(zero origin)
     * @return the byte array of the specified row
     */
    byte[] get(int row);

    /**
     * Usually use {@link Document#getText(int)}.
     * Gets the char sequence at the specified position.
     * @param row the number of row(zero origin)
     * @param rawCol the byte position on the row (The value must be encoded in the appropriate Charset)
     * @param rawLen the byte length (The value must be encoded in the appropriate Charset)
     * @return the char sequence
     */
    CharSequence getText(int row, int rawCol, int rawLen);

    /**
     * Set readonly.
     * @param readonly readonly
     */
    void readonly(boolean readonly);

    /**
     * Get readonly.
     * @return readonly
     */
    boolean readonly();

    /**
     * Get the row size.
     * @return the row size
     */
    int rows();

    /**
     * Get the byte length of this document holds.
     * Not the java's UTF-16 encoded memory size.
     * @return the byte length of this document holds
     */
    long rawSize();

    /**
     * Get the serial position.
     * Represents the byte position from the beginning of the file.
     * This position does not include bom.
     * @param row the specified row
     * @param col the specified position in a row
     * @return the serial position
     */
    long serial(int row, int col);

    /**
     * Get the position from the specified serial.
     * @param serial the serial position
     * @return the row-col position
     */
    Pos pos(long serial);

    /**
     * Get the charset.
     * @return the charset
     */
    Charset charset();

    /**
     * Get the row ending.
     * @return the row ending
     */
    RowEnding rowEnding();

    /**
     * Get the bom.
     * @return the bom, if there is no bom, an empty byte array
     */
    byte[] bom();

    /**
     * Get the path.
     * @return the path, otherwise, otherwise {@code null} if the original file does not exist
     */
    Path path();

    /**
     * Save this document.
     * @param path the path
     */
    void save(Path path);

    /**
     * Writes the contents of a document to the specified path.
     * This method is intended for backup path creation and other uses.
     * @param path the specified path
     */
    void write(Path path);

    /**
     * Closes the document and releases any associated resources.
     */
    void close();

    /**
     * Get the {@link SearchContext}.
     * @return the {@link SearchContext}
     */
    SearchContext search();

    /**
     * Create a new {@link Document}.
     * @return a new {@link Document}
     */
    static Document of() {
        return DocumentImpl.of();
    }

    /**
     * Create a new {@link Document}.
     * To specify charset, use the following.
     * {@snippet :
     * var doc = Document.of(path, CharsetMatch.of(StandardCharsets.UTF_8));
     * }
     * @param path the path of the file to read
     * @param charsetMatches the charset matches
     * @return a new {@link Document}
     */
    static Document of(Path path, CharsetMatch... charsetMatches) {
        return DocumentImpl.of(path, charsetMatches);
    }

    /**
     * Create a new {@link Document} from the specified byte array.
     * @param bytes the specified byte array
     * @param charsetMatches the charset matches
     * @return a new {@link Document}
     */
    static Document of(byte[] bytes, CharsetMatch... charsetMatches) {
        return DocumentImpl.of(bytes, charsetMatches);
    }

    /**
     * Create a new {@link Document}.
     * @param path the {@link Path} of the document
     * @param listener the progress listener
     * @param charsetMatches the charset matches
     * @return a new {@link Document}
     */
    static Document of(Path path, Consumer<Segment> listener, CharsetMatch... charsetMatches) {
        return DocumentImpl.of(path, listener, charsetMatches);
    }

}
