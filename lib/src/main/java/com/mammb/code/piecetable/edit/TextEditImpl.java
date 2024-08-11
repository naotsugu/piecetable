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
package com.mammb.code.piecetable.edit;

import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.TextEdit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The document edit.
 * @author Naotsugu Kobayashi
 */
public class TextEditImpl implements TextEdit {

    /** The edit queue. */
    private final Deque<Edit> deque = new ArrayDeque<>();

    /** The undo queue. */
    private final Deque<Edit> undo = new ArrayDeque<>();

    /** The redo queue. */
    private final Deque<Edit> redo = new ArrayDeque<>();

    /** The dry run buffer. */
    private final Map<Integer, String> dryBuffer = new HashMap<>();

    /** The document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public TextEditImpl(Document doc) {
        this.doc = doc;
    }

    @Override
    public void insert(int row, int col, String text) {
        Edit e;
        int index = text.lastIndexOf('\n');
        if (index < 0) {
            e = new Edit.Ins(
                new Pos(row, col),
                new Pos(row, col + text.length()),
                text,
                System.currentTimeMillis());
        } else {
            int nRow = row + (int) text.chars().mapToLong(c -> c == '\n' ? 1 : 0).sum();
            int nCol = text.substring(index + 1).length();
            e = new Edit.Ins(
                new Pos(row, col),
                new Pos(nRow, nCol),
                text,
                System.currentTimeMillis());
        }
        push(e);
    }

    @Override
    public void delete(int row, int col, int len) {
        String rowText = getText(row);
        Edit e;
        if (rowText.length() > col + len) {
            e = new Edit.Del(
                new Pos(row, col),
                new Pos(row, col),
                rowText.substring(col, col + len),
                System.currentTimeMillis());
        } else {
            StringBuilder sb = new StringBuilder(rowText.substring(col));
            for (int nRow = row + 1; nRow < doc.rows(); nRow++) {
                rowText = getText(nRow);
                if (len - rowText.length() <= 0) {
                    sb.append(rowText, 0, len);
                    break;
                }
                len -= rowText.length();
                nRow++;
                sb.append(rowText);
            }
            e = new Edit.Del(
                new Pos(row, col),
                new Pos(row, col),
                sb.toString(),
                System.currentTimeMillis());
        }
        push(e);
    }

    @Override
    public void backspace(int row, int col, int len) {
        String rowText = getText(row);
        Edit e;
        if (col - len >= 0) {
            e = new Edit.Del(
                new Pos(row, col - len),
                new Pos(row, col),
                rowText.substring(col - len, len),
                System.currentTimeMillis());
        } else {
            StringBuilder sb = new StringBuilder(rowText.substring(0, col));
            int nRow = row - 1;
            int nCol;
            len -= col;
            for (;;) {
                rowText = getText(nRow);
                if (len - rowText.length() <= 0) {
                    nCol = rowText.length() - len;
                    sb.insert(0, rowText.substring(nCol));
                    break;
                }
                len -= rowText.length();
                nRow--;
                sb.insert(0, rowText);
            }
            e = new Edit.Del(
                new Pos(row, col),
                new Pos(nRow, nCol),
                sb.toString(),
                System.currentTimeMillis());
        }
        push(e);
    }


    @Override
    public String getText(int row) {
        if (!deque.isEmpty() && dryBuffer.isEmpty()) {
            dryApply();
        }
        if (dryBuffer.containsKey(row)) {
            return dryBuffer.get(row);
        }
        return doc.getText(row).toString();
    }

    @Override
    public List<Pos> undo() {
        Optional<Edit> e = undoEdit();
        return List.of();
    }

    @Override
    public List<Pos> redo() {
        Optional<Edit> e = redoEdit();
        return List.of();
    }

    @Override
    public void flush() {
        while (!deque.isEmpty()) {
            Edit edit = deque.pop();
            apply(edit);
            undo.push(edit.flip());
            redo.clear();
        }
        dryBuffer.clear();
    }

    @Override
    public void clear() {
        flush();
        undo.clear();
        redo.clear();
    }

    /**
     * Push the edit.
     * @param edit the edit
     */
    private void push(final Edit edit) {
        Edit last = deque.peekLast();
        if (last != null) {
            Optional<Edit> merged = last.merge(edit);
            if (merged.isPresent()) {
                deque.removeLast();
                flush();
                deque.push(merged.get());
                return;
            }
        }
        flush();
        deque.push(edit);

        switch (edit) {
            // Multi-row edit is not supported at this time.
            case Edit.ConcreteEdit e when e.text().contains("\n") -> flush();
            case Edit.Cmp e when e.edits().stream().map(Edit.ConcreteEdit::text).anyMatch(s -> s.contains("\n")) -> flush();
            default -> { }
        }
    }

    private Optional<Edit> undoEdit() {
        flush();
        if (undo.isEmpty()) return Optional.empty();
        Edit edit = undo.pop();
        apply(edit);
        redo.push(edit.flip());
        return Optional.of(edit);
    }

    private Optional<Edit> redoEdit() {
        flush();
        if (redo.isEmpty()) return Optional.empty();
        Edit edit = redo.pop();
        apply(edit);
        undo.push(edit.flip());
        return Optional.of(edit);
    }

    private void apply(Edit edit) {
        switch (edit) {
            case Edit.Ins e -> doc.insert(e.min().row(), e.min().col(), e.text());
            case Edit.Del e -> doc.delete(e.min().row(), e.min().col(), e.text().length());
            case Edit.Cmp e -> e.edits().forEach(this::apply);
        }
    }

    private void dryApply() {
        dryBuffer.clear();
        deque.forEach(this::dryApply);
    }

    private void dryApply(Edit edit) {
        switch (edit) {
            case Edit.Ins e -> {
                String row = dryBuffer.get(e.from().row());
                if (row == null) {
                    row = doc.getText(e.from().row()).toString();
                }
                row = row.substring(0, e.min().col()) + e.text() + row.substring(e.min().col());
                dryBuffer.put(e.from().row(), row);
            }
            case Edit.Del e -> {
                String row = dryBuffer.get(e.from().row());
                if (row == null) {
                    row = doc.getText(e.from().row()).toString();
                }
                row = row.substring(0, e.min().col()) + row.substring(e.min().col() + e.text().length());
                dryBuffer.put(e.from().row(), row);
            }
            case Edit.Cmp e -> e.edits().forEach(this::dryApply);
        }
    }

//    static Edit replace(int fromRow, int fromCol, int toRow, int toCol, String beforeText, String afterText) {
//        long occurredOn = System.currentTimeMillis();
//        boolean forward = (fromRow < toRow || (fromRow == toRow && fromCol < toCol));
//        if (forward) {
//            //  |0|1|2|3|4|5|
//            //    ^     ^
//            //    |     |
//            //    |     caret(to)
//            //    select start(from)
//            return new Cmp(List.of(
//                new Del(TextEditx.Range.leftOf(toRow, toCol), beforeText, occurredOn),
//                new Ins(TextEditx.Range.leftOf(fromRow, fromCol), afterText, occurredOn)), occurredOn);
//        } else {
//            //  |0|1|2|3|4|5|
//            //    ^     ^
//            //    |     |
//            //    |     select start(from)
//            //    caret(to)
//            return new Cmp(List.of(
//                new Del(TextEditx.Range.rightOf(fromRow, fromCol), beforeText, occurredOn),
//                new Del(TextEditx.Range.rightOf(fromRow, fromCol), beforeText, occurredOn)), occurredOn);
//        }
//    }
}
