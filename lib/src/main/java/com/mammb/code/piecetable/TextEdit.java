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

import com.mammb.code.piecetable.edit.TextEditImpl;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The text edit.
 * Provides an abstraction for the text edit to be edited.
 * Represents the contract for a {@code TextEdit} that provides various functionalities
 * needed for handling, editing, and managing textual and binary content using rows and columns.
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
     * Backspace deletes the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @return the new position
     */
    Pos backspace(int row, int col);

    /**
     * Backspace deletes the text from this {@code TextEdit}.
     * @param row the number of row(zero origin)
     * @param col the byte position on the row where the text to be deleted
     * @param len the byte length to be deleted
     * @return the new position
     */
    Pos backspace(int row, int col, int len);

    /**
     * Backspace deletes the multi text from this {@code TextEdit}.
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
     * Replace the multi text from this {@code TextEdit}.
     * @param requests the replacement requests
     * @return the new position
     */
    List<Range> replace(List<Replace> requests);

    /**
     * Replace the text in the specified range from this {@code TextEdit}.
     * @param startRow the range start row
     * @param startCol the range start col
     * @param endRow the range end row
     * @param endCol the range end col
     * @param convert the function to convert the original text to the replaced text
     * @return the new position
     */
    default Pos replace(int startRow, int startCol, int endRow, int endCol, Convert convert) {
        return replace(new Pos(startRow, startCol), new Pos(endRow, endCol), convert);
    }

    /**
     * Replace the text in the specified range from this {@code TextEdit}.
     * @param start the start position
     * @param end the end position
     * @param convert the function to convert the original text to the replaced text
     * @return the new position
     */
    default Pos replace(Pos start, Pos end, Convert convert) {
        String text = getText(start, end);
        return replace(
            start.row(), start.col(),
            text.length() * (int) Math.signum(end.compareTo(start)),
            convert.apply(text));
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
     * Get the row size.
     * @return the row size
     */
    int rows();

    /**
     * Get the byte length of this document holds.
     * Not the javas UTF-16 encoded memory size.
     * @return the byte length of this document holds
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
     * Get the bom as byte array.
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
     * Closes the current {@code TextEdit} instance and releases any system resources associated with it.
     */
    void close();

    /**
     * Get the {@link SearchContext}.
     * @return the {@link SearchContext}
     */
    SearchContext search();

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
        return new TextEditImpl(Document.of(path));
    }

    /**
     * Create a new {@link TextEdit}.
     * @param path the path of the file to read
     * @param listener the progress listener
     * @return a new {@link TextEdit}
     */
    static TextEdit of(Path path, Consumer<Segment> listener) {
        return new TextEditImpl(Document.of(path, listener));
    }

    /**
     * Create a new {@link TextEdit} from the specified byte array.
     * @param bytes the specified byte array
     * @return a new {@link TextEdit}
     */
    static TextEdit of(byte[] bytes) {
        return new TextEditImpl(Document.of(bytes));
    }

    /**
     * The function to convert the original text to the replaced text.
     */
    interface Convert extends Function<String, String> {
        /**
         * Apply the function
         * @param source the source text
         * @return the converted text
         */
        @Override
        String apply(String source);
    }

    /**
     * The replace request.
     * @param markPos the end point(marked point)
     * @param caretPos the start point(caret point)
     * @param convert the convert
     */
    record Replace(Pos markPos, Pos caretPos, Convert convert) implements Comparable<Replace> {

        /**
         * Create a new replacement request.
         * @param caretPos the start point(caret point)
         * @param markPos the end point(marked point)
         * @param text the text to be replaced
         * @return a new replacement request
         */
        public static Replace of(Pos markPos, Pos caretPos, String text) {
            return new Replace(markPos, caretPos, _ -> text);
        }

        /**
         * Get the min pos.
         * @return the min pos
         */
        public Pos min() {
            return caretPos.compareTo(markPos) < 0 ? caretPos : markPos;
        }
        /**
         * Get the max pos.
         * @return the max pos
         */
        public Pos max() {
            return caretPos.compareTo(markPos) > 0 ? caretPos : markPos;
        }

        @Override
        public int compareTo(Replace that) {
            return min().compareTo(that.min());
        }

    }

}
