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

    public void flush() {
        while (!deque.isEmpty()) {
            Edit edit = deque.pop();
            if (edit == Edit.empty) continue;
            apply(edit);
            undo.push(edit.flip());
            redo.clear();
        }
    }

    public Edit undo() {
        flush();
        if (undo.isEmpty()) return Edit.empty;
        Edit edit = undo.pop();
        apply(edit);
        redo.push(edit.flip());
        return edit;
    }

    public Edit redo() {
        flush();
        if (redo.isEmpty()) return Edit.empty;
        Edit edit = redo.pop();
        apply(edit);
        undo.push(edit.flip());
        return edit;
    }

    private void apply(Edit edit) {
        switch (edit) {
            case Edit.Ins ins -> doc.insert(ins.row(), ins.col(), ins.text());
            case Edit.Del del -> doc.delete(del.row(), del.col(), del.text().length());
            default -> { }
        }
    }

}
