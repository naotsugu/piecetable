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
package com.mammb.code.piecetable.editx;

import com.mammb.code.piecetable.Document;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.mammb.code.piecetable.TextEditx;

/**
 * The document edit.
 * @author Naotsugu Kobayashi
 */
public class TextEditxImpl implements TextEditx {

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
    public TextEditxImpl(Document doc) {
        this.doc = doc;
    }

    @Override
    public void insert(int row, int col, String text) {
        push(Edit.insert(row, col, text));
    }

    @Override
    public void delete(int row, int col, int len) {
        push(Edit.delete(row, col, getText(row).substring(col, col + len)));
    }

    @Override
    public void backspace(int row, int col, int len) {
        push(Edit.backspace(row, col, getText(row).substring(col - len, col)));
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
    public Optional<Range> undo() {
        Optional<Edit> e = undoEdit();
        return Optional.empty();
    }

    @Override
    public Optional<Range> redo() {
        Optional<Edit> e = redoEdit();
        return Optional.empty();
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
System.out.println("  " + edit);
        switch (edit) {
            case Edit.Ins e -> doc.insert(e.range().row(), e.range().col(), e.text());
            case Edit.Del e when e.range().right() -> doc.delete(e.range().row(), e.range().col(), e.text().length());
            case Edit.Del e -> doc.delete(e.range().row(), e.range().col() - e.text().length(), e.text().length());
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
                String row = dryBuffer.get(e.range().row());
                if (row == null) {
                    row = doc.getText(e.range().row()).toString();
                }
                row = row.substring(0, e.range().col()) + e.text() + row.substring(e.range().col());
                dryBuffer.put(e.range().row(), row);
            }
            case Edit.Del e -> {
                String row = dryBuffer.get(e.range().row());
                if (row == null) {
                    row = doc.getText(e.range().row()).toString();
                }
                if (e.range().left()) {
                    row = row.substring(0, e.range().col() - e.text().length()) + row.substring(e.range().col());
                } else {
                    row = row.substring(0, e.range().col()) + row.substring(e.range().col() + e.text().length());
                }
                dryBuffer.put(e.range().row(), row);
            }
            case Edit.Cmp e -> e.edits().forEach(this::dryApply);
        }
    }

}
