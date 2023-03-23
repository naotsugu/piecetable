/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.model;

import com.mammb.code.editor.Strings;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The edit queue.
 * @author Naotsugu Kobayashi
 */
public class EditQueue {

    /** The edit listener. */
    private final EditListener handler;

    /** The buffered edit. */
    private Edit buffer = Edit.empty;

    /** The undo queue. */
    private final Deque<Edit> undo = new ArrayDeque<>();

    /** The redo queue. */
    private final Deque<Edit> redo = new ArrayDeque<>();


    /**
     * Constructor.
     * @param handler the edit listener
     */
    public EditQueue(EditListener handler) {
        this.handler = handler;
    }


    /**
     * Push the edit.
     * @param edit the edit
     */
    public void push(Edit edit) {
        if (buffer.canMarge(edit)) {
            buffer = buffer.marge(edit);
        } else {
            flush();
            buffer = edit;
        }
    }


    /**
     * Undo.
     * @return the undone edit.
     */
    public Edit undo() {
        flush();
        if (undo.isEmpty()) return Edit.empty;
        Edit edit = undo.pop();
        handler.handle(edit);
        redo.push(edit.flip());
        return edit;
    }


    /**
     * Redo.
     * @return the redone edit.
     */
    public Edit redo() {
        flush();
        if (redo.isEmpty()) return Edit.empty;
        Edit edit = redo.pop();
        handler.handle(edit);
        undo.push(edit.flip());
        return edit;
    }


    /**
     * Get the pending code point count delta.
     * @return the pending code point count delta
     */
    public int pendingCodePointCountDelta() {
        return buffer.isDelete() ? -buffer.codePointCount()
             : buffer.isInsert() ? +buffer.codePointCount()
             : 0;
    }


    /**
     * Get the pending lf count delta.
     * @return the pending lf count delta
     */
    public int pendingLfCountDelta() {
        return buffer.isDelete() ? -Strings.countLf(buffer.string())
             : buffer.isInsert() ? +Strings.countLf(buffer.string())
             : 0;
    }


    /**
     * Clear edit queue.
     */
    public void clear() {
        flush();
        undo.clear();
        redo.clear();
    }


    /**
     * Flush edit queue.
     */
    public void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        handler.handle(buffer);
        undo.push(buffer.flip());
        redo.clear();
        buffer = Edit.empty;
    }

}
