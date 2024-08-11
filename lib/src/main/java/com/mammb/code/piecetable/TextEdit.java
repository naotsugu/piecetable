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

import com.mammb.code.piecetable.edit.TextEditImpl;
import com.mammb.code.piecetable.text.DocumentImpl;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * The text edit.
 * @author Naotsugu Kobayashi
 */
public interface TextEdit {

    /**
     * Inserts the text into this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text is to be inserted
     * @param text the char sequence is to be inserted
     */
    void insert(int row, int col, String text);

    /**
     * Delete the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @param len the byte length to be deleted
     */
    void delete(int row, int col, int len);

    /**
     * Backspace delete the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @param len the byte length to be deleted
     */
    void backspace(int row, int col, int len);

    /**
     * Replace the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be replaced
     * @param len the byte length to be replaced
     * @param text the char sequence is to be inserted
     */
    void replace(int row, int col, int len, String text);

    /**
     * Gets the text at the specified row.
     * @param row the number of row(zero origin)
     * @return the text of the specified row
     */
    String getText(int row);

    /**
     * Undo.
     * @return the undo position
     */
    List<Pos> undo();

    /**
     * Redo.
     * @return the redo position
     */
    List<Pos> redo();

    /**
     * Flush.
     */
    void flush();

    /**
     * Clear undo/redo buffer.
     */
    void clear();

    /**
     * Searches for the specified char sequence.
     * @param text the specified char sequence
     * @return found list
     */
    List<Found> findAll(String text);

    /**
     * Searches for the specified char sequence.
     * @param text the specified char sequence
     * @param row the number of start row(zero origin)
     * @param col the start byte position on the row
     * @return found
     */
    Optional<Found> findNext(String text, int row, int col);

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
     * Create a new {@link TextEdit}.
     * @return a new {@link TextEdit}
     */
    static TextEdit of() {
        return new TextEditImpl(Document.of());
    }

    /**
     * Create a new {@link TextEdit}.
     * @param path the path of the file to read
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Path path) {
        return new TextEditImpl(DocumentImpl.of(path));
    }

    /**
     * Create a new {@link TextEdit}.
     * @param document the Document
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Document document) {
        return new TextEditImpl(document);
    }

    /**
     * The position record.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row
     */
    record Pos(int row, int col) { }

}
