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
import com.mammb.code.piecetable.Found;
import com.mammb.code.piecetable.TextEdit;
import java.nio.charset.Charset;
import java.nio.file.Path;
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
        push(insertEdit(row, col, text, System.currentTimeMillis()));
    }

    @Override
    public String delete(int row, int col, int len) {
        Edit.Del e = deleteEdit(row, col, len, System.currentTimeMillis());
        push(e);
        return e.text();
    }

    @Override
    public String backspace(int row, int col, int len) {
        Edit.Del e = backspaceEdit(row, col, len, System.currentTimeMillis());
        push(e);
        return e.text();
    }

    @Override
    public String replace(int row, int col, int len, String text) {
        long occurredOn = System.currentTimeMillis();
        Edit e;
        String ret = "";
        if (len == 0) {
            e = insertEdit(row, col, text, occurredOn);
        } else if (len > 0) {
            Edit.ConcreteEdit del = deleteEdit(row, col, len, occurredOn);
            e = new Edit.Cmp(List.of(del, insertEdit(row, col, text, occurredOn)), occurredOn);
            ret = del.text();
        } else {
            Edit.ConcreteEdit bs = backspaceEdit(row, col, -len, occurredOn);
            e = new Edit.Cmp(List.of(bs, insertEdit(bs.to().row(), bs.to().col(), text, occurredOn)), occurredOn);
            ret = bs.text();
        }
        push(e);
        return ret;
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
        final Optional<Edit> undo = undoEdit();
        if (undo.isEmpty()) {
            return List.of();
        }
        return switch (undo.get()) {
            case Edit.ConcreteEdit e -> List.of(e.to());
            case Edit.Cmp e -> e.edits().stream().map(Edit.ConcreteEdit::to).toList();
        };
    }

    @Override
    public List<Pos> redo() {
        final Optional<Edit> redo = redoEdit();
        if (redo.isEmpty()) {
            return List.of();
        }
        return switch (redo.get()) {
            case Edit.ConcreteEdit e -> List.of(e.to());
            case Edit.Cmp e -> e.edits().stream().map(Edit.ConcreteEdit::to).toList();
        };
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

    @Override
    public List<Found> findAll(String text) {
        return doc.findAll(text);
    }

    @Override
    public Optional<Found> findNext(String text, int row, int col) {
        return doc.findNext(text, row, col);
    }

    @Override
    public int rows() {
        return doc.rows();
    }

    @Override
    public long length() {
        return doc.length();
    }

    @Override
    public Charset charset() {
        return doc.charset();
    }

    @Override
    public Path path() {
        return doc.path();
    }

    @Override
    public void save(Path path) {
        doc.save(path);
    }

    Edit.Ins insertEdit(int row, int col, String text, long occurredOn) {
        int index = text.lastIndexOf('\n');
        if (index < 0) {
            return new Edit.Ins(
                new Pos(row, col),
                new Pos(row, col + text.length()),
                text,
                occurredOn);
        } else {
            int nRow = row + (int) text.chars().mapToLong(c -> c == '\n' ? 1 : 0).sum();
            int nCol = text.substring(index + 1).length();
            return new Edit.Ins(
                new Pos(row, col),
                new Pos(nRow, nCol),
                text,
                occurredOn);
        }
    }

    Edit.Del deleteEdit(int row, int col, int len, long occurredOn) {
        String rowText = getText(row);
        if (rowText.length() > col + len) {
            return new Edit.Del(
                new Pos(row, col),
                new Pos(row, col),
                rowText.substring(col, col + len),
                occurredOn);
        } else {
            StringBuilder sb = new StringBuilder(rowText.substring(col));
            len -= sb.length();
            for (int nRow = row + 1; nRow < doc.rows(); nRow++) {
                rowText = getText(nRow);
                if (len - rowText.length() <= 0) {
                    sb.append(rowText, 0, len);
                    break;
                }
                len -= rowText.length();
                sb.append(rowText);
            }
            return new Edit.Del(
                new Pos(row, col),
                new Pos(row, col),
                sb.toString(),
                occurredOn);
        }
    }

    Edit.Del backspaceEdit(int row, int col, int len, long occurredOn) {
        String rowText = getText(row);
        if (col - len >= 0) {
            return new Edit.Del(
                new Pos(row, col),
                new Pos(row, col - len),
                rowText.substring(col - len, col),
                occurredOn);
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
            return new Edit.Del(
                new Pos(row, col),
                new Pos(nRow, nCol),
                sb.toString(),
                occurredOn);
        }
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

    Document getDoc() { return doc; }
    Map<Integer, String> getDryBuffer() { return dryBuffer; }
    Deque<Edit> getDeque() { return deque; }
    Deque<Edit> getUndo() { return undo; }
    Deque<Edit> getRedo() { return redo; }
    void testPush(final Edit edit) { push(edit); }
    void testDryApply() { dryApply(); }
    void testDryApply(Edit edit) { dryApply(edit); }

}
