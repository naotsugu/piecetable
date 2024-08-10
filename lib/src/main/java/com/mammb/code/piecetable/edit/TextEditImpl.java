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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import com.mammb.code.piecetable.TextEdit;

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

    /** The document. */
    private final Document doc;


    public TextEditImpl(Document doc) {
        this.doc = doc;
    }

    /**
     * Push the edit.
     * @param edit the edit
     */
    public void push(Edit edit) {
        Edit last = deque.peekLast();
        if (last != null) {
            Optional<Edit> merge = last.merge(edit);
            if (merge.isPresent()) {
                deque.removeLast();
                edit = merge.get();
            }
        }
        if (!deque.isEmpty()) {
            flush();
        }
        deque.push(edit);
        //if (edit.acrossRows()) {
        //    flush();
        //}
    }

    public void flush() {
        while (!deque.isEmpty()) {
            Edit edit = deque.pop();
            apply(edit);
            undo.push(edit.flip());
            redo.clear();
        }
    }

    public Optional<Edit> undo() {
        flush();
        if (undo.isEmpty()) return Optional.empty();
        Edit edit = undo.pop();
        apply(edit);
        redo.push(edit.flip());
        return Optional.of(edit);
    }

    public Optional<Edit> redo() {
        flush();
        if (redo.isEmpty()) return Optional.empty();
        Edit edit = redo.pop();
        apply(edit);
        undo.push(edit.flip());
        return Optional.of(edit);
    }

    private void apply(Edit edit) {
        switch (edit) {
            case Edit.Ins e -> doc.insert(e.range().row(), e.range().col(), e.text());
            case Edit.Del e when e.range().right() -> doc.delete(e.range().row(), e.range().col(), e.text().length());
            case Edit.Del e when e.range().left()  -> doc.delete(e.range().row(), e.range().col() - e.text().length(), e.text().length());
            default -> { }
        }
    }
}
