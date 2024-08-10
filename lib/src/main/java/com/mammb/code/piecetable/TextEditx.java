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

import com.mammb.code.piecetable.editx.TextEditxImpl;
import com.mammb.code.piecetable.text.DocumentImpl;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The text edit.
 * @author Naotsugu Kobayashi
 */
public interface TextEditx {

    /**
     * Create a new {@link TextEditx}.
     * @return a new {@link TextEditx}
     */
    static TextEditx of() {
        return new TextEditxImpl(Document.of());
    }

    /**
     * Create a new {@link TextEditx}.
     * @param path the path of the file to read
     * @return a new {@link TextEditx}
     */
    static TextEditx of(Path path) {
        return new TextEditxImpl(DocumentImpl.of(path));
    }

    /**
     * Create a new {@link TextEditx}.
     * @param document the Document
     * @return a new {@link TextEditx}
     */
    static TextEditx of(Document document) {
        return new TextEditxImpl(document);
    }

    void insert(int row, int col, String text);

    void delete(int row, int col, int len);

    void backspace(int row, int col, int len);

    String getText(int row);

    Optional<Range> undo();

    Optional<Range> redo();

    void flush();

    void clear();

    /**
     * |0|1|2|3|4|
     *   ^ 1..-1
     *
     * |0|1|2|3|4|
     *   ^^^^^^ 1..4
     * @param fromRow
     * @param fromCol
     * @param toRow
     * @param toCol
     * @param left
     */
    record Range(int fromRow, int fromCol, int toRow, int toCol, boolean left) {
        public Range {
            if (fromRow < 0 || fromCol < 0 || toRow < 0 || toCol < 0)
                throw new IllegalArgumentException(String.format("(%d, %d)(%d, %d)", fromRow, fromCol, toRow, toCol));
        }

        public static Range leftOf(int row, int col) { return new Range(row, col, row, col, true); }
        public static Range rightOf(int row, int col) { return new Range(row, col, row, col, false); }

        public Range flip() { return new Range(fromRow, fromCol, toRow, toCol, !left); }
        public int row() { return left ? toRow : fromRow; }
        public int col() { return left ? toCol : fromCol; }
        public boolean right() { return !left; }
        public boolean mono() { return fromRow == toRow && fromCol == toCol; }
    }

}
