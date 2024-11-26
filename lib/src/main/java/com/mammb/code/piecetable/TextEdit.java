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

import com.mammb.code.piecetable.Document.RowEnding;
import com.mammb.code.piecetable.edit.TextEditImpl;
import com.mammb.code.piecetable.text.DocumentImpl;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * The text edit.
 * Provides an abstraction of text editing.
 * @author Naotsugu Kobayashi
 */
public interface TextEdit {

    /**
     * Inserts the text into this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text is to be inserted
     * @param text the char sequence is to be inserted
     * @return the new position
     */
    Pos insert(int row, int col, String text);

    /**
     * Inserts the multi text from this {@code TextEdit}.
     * @param posList the position list
     * @param text the char sequence is to be inserted
     * @return the new position
     */
    List<Pos> insert(List<Pos> posList, String text);

    /**
     * Delete the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @return the deleted text
     */
    String delete(int row, int col);

    /**
     * Delete the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @param len the byte length to be deleted
     * @return the deleted text
     */
    String delete(int row, int col, int len);

    /**
     * Delete the multi text from this {@code TextEdit}.
     * @param posList the position list
     * @return the new position
     */
    List<Pos> delete(List<Pos> posList);

    /**
     * Backspace delete the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @return the new position
     */
    Pos backspace(int row, int col);

    /**
     * Backspace delete the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @param len the byte length to be deleted
     * @return the new position
     */
    Pos backspace(int row, int col, int len);

    /**
     * Backspace delete the multi text from this {@code TextEdit}.
     * @param posList the position list
     * @return the new position
     */
    List<Pos> backspace(List<Pos> posList);

    /**
     * Replace the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be replaced
     * @param len the byte length to be replaced
     * @param text the char sequence is to be inserted
     * @return the new position
     */
    Pos replace(int row, int col, int len, String text);

    /**
     * @deprecated
     * Replace the text in the specified range from this {@code TextEdit}.
     * @param startRow the range start row
     * @param startCol the range start col
     * @param endRow the range end row
     * @param endCol the range end col
     * @param text the char sequence is to be inserted
     * @return the new position
     */
    @Deprecated
    default Pos replace(int startRow, int startCol, int endRow, int endCol, String text) {
        return replace(new Pos(startRow, startCol), new Pos(endRow, endCol), text);
    }

    /**
     * @deprecated
     * Replace the text in the specified range from this {@code TextEdit}.
     * @param start the start position
     * @param end the end position
     * @param text the char sequence is to be inserted
     * @return the new position
     */
    @Deprecated
    default Pos replace(Pos start, Pos end, String text) {
        return replace(start, end, org -> text);
    }

    /**
     * Replace the text in the specified range from this {@code TextEdit}.
     * @param startRow the range start row
     * @param startCol the range start col
     * @param endRow the range end row
     * @param endCol the range end col
     * @param fun the function to convert the original text to the replaced text
     * @return the new position
     */
    default Pos replace(int startRow, int startCol, int endRow, int endCol, Function<String, String> fun) {
        return replace(new Pos(startRow, startCol), new Pos(endRow, endCol), fun);
    }

    /**
     * Replace the text in the specified range from this {@code TextEdit}.
     * @param start the start position
     * @param end the end position
     * @param fun the function to convert the original text to the replaced text
     * @return the new position
     */
    default Pos replace(Pos start, Pos end, Function<String, String> fun) {
        String text = getText(start, end);
        return replace(
            start.row(), start.col(),
            text.length() * (int) Math.signum(end.compareTo(start)),
            fun.apply(text));
    }

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
     * Gets whether there is an undo record.
     * @return {@code true} if there is an undo record
     */
    boolean hasUndoRecord();

    /**
     * Gets the text at the specified row.
     * @param row the number of row(zero origin)
     * @return the text of the specified row
     */
    String getText(int row);

    /**
     * Gets the text at the specified row range.
     * @param startRow the start row
     * @param endRowExclusive the end row
     * @return the text of the specified rows
     */
    String getText(int startRow, int endRowExclusive);

    /**
      * Gets the text at the specified range.
      * @param startRow the start row
      * @param startCol the start col
      * @param endRow the end row
      * @param endCol the end col
      * @return the text of the specified range
      */
    default String getText(int startRow, int startCol, int endRow, int endCol) {
        return getText(new Pos(startRow, startCol), new Pos(endRow, endCol));
    }

    /**
     * Gets the text list at the specified range.
     * @param start the start pos
     * @param end the end pos
     * @return the text of the specified range
     */
    String getText(Pos start, Pos end);

    /**
     * Gets the text list at the specified range.
     * @param start the start pos
     * @param end the end pos
     * @return the text list of the specified range
     */
    List<String> getTexts(Pos start, Pos end);

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
     * Not the javas UTF-16 encoded memory size.
     * @return the bytes length of this document holds
     */
    long rawSize();

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
     * @param path the path of the file to read
     * @param traverseCallback the document traverse callback
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Path path, Function<byte[], Boolean> traverseCallback) {
        return new TextEditImpl(DocumentImpl.of(path, traverseCallback));
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
    record Pos(int row, int col) implements Comparable<Pos> {
        @Override
        public int compareTo(Pos that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }

    /**
     * The range of points
     * @param from the position of from
     * @param to the position of to
     */
    record Range(Pos from, Pos to) { }

}
