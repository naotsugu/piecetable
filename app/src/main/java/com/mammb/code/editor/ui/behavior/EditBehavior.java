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
package com.mammb.code.editor.ui.behavior;

import com.mammb.code.editor.lang.EventListener;
import com.mammb.code.editor.lang.Strings;
import com.mammb.code.editor.model.OffsetPoint;
import com.mammb.code.editor.model.TextModel;
import com.mammb.code.editor.ui.Pointing;
import com.mammb.code.editor.ui.TextFlow;
import com.mammb.code.editor.ui.util.Clipboards;
import com.mammb.code.editor.ui.util.Texts;
import java.lang.System.Logger;
import java.lang.System.Logger.*;

/**
 * InputBehavior.
 * @author Naotsugu Kobayashi
 */
public class EditBehavior {

    /** The logger. */
    private static final Logger log = System.getLogger(EditBehavior.class.getName());

    /** The text model. */
    private final TextModel model;

    /** The pointing. */
    private final Pointing pointing;

    /** The text flow pane. */
    private final TextFlow textFlow;

    /** The caret behavior. */
    private final CaretBehavior caretBehavior;

    /** The scroll behavior. */
    private final ScrollBehavior scrollBehavior;

    /** The edit listener. */
    private final EventListener<String> editListener;


    /**
     * Constructor.
     * @param model the text model
     * @param pointing the pointing
     * @param textFlow the text flow pane
     * @param caretBehavior the caret behavior
     * @param scrollBehavior the scroll behavior
     * @param editListener the scroll behavior
     */
    public EditBehavior(TextModel model, Pointing pointing,
            TextFlow textFlow,
            CaretBehavior caretBehavior,
            ScrollBehavior scrollBehavior,
            EventListener<String> editListener) {
        this.model = model;
        this.pointing = pointing;
        this.textFlow = textFlow;
        this.caretBehavior = caretBehavior;
        this.scrollBehavior = scrollBehavior;
        this.editListener = editListener;
    }


    /**
     * Insert text.
     * @param value the insertion text
     */
    public void input(String value) {
        if (pointing.selectionOn()) {
            selectionDelete();
        }

        if (value.contains("\r")) {
            // Enter key : 13:CR -> replace to 10:LF
            value = Strings.unifyLf(value);
        }
        caretBehavior.at();
        if (pointing.caretOffset() >= (model.tailOffset() - model.originOffset())) {
            // pre scroll if additions to the tail
            int lfCount = Strings.countLf(value);
            if (lfCount > 0) {
                scrollBehavior.scrollNext(lfCount);
            }
        }
        model.add(pointing.caretOffset(), value);
        textFlow.setAll(Texts.asText(model.text()));
        for (int i = 0; i < Strings.codePointCount(value); i++)
            caretBehavior.right();

        editListener.handle("input");
    }


    /**
     * Delete text.
     */
    public void delete() {
        if (pointing.selectionOn()) {
            selectionDelete();
        } else {
            caretBehavior.at();
            model.delete(pointing.caretOffset(), 1);
        }
        textFlow.setAll(Texts.asText(model.text()));
        caretBehavior.refresh();
        editListener.handle("delete");
    }


    /**
     * Delete text with backspace.
     */
    public void backspace() {
        if (pointing.selectionOn()) {
            selectionDelete();
            caretBehavior.at();
        } else {
            caretBehavior.at();
            caretBehavior.left();
            model.delete(pointing.caretOffset(), 1);
        }
        textFlow.setAll(Texts.asText(model.text()));
        caretBehavior.refresh();
        editListener.handle("backspace");
    }


    /**
     * Undo.
     */
    public void undo() {
        OffsetPoint peek = model.undoPeek();
        if (peek.isEmpty()) return;
        caretBehavior.at(peek);

        pointing.clearSelection();
        model.undo();
        textFlow.setAll(Texts.asText(model.text()));
        caretBehavior.refresh();
        editListener.handle("undo");
    }


    /**
     * Redo.
     */
    public void redo() {
        OffsetPoint peek = model.redoPeek();
        if (peek.isEmpty()) return;
        caretBehavior.at(peek);

        pointing.clearSelection();
        model.redo();
        textFlow.setAll(Texts.asText(model.text()));
        caretBehavior.refresh();
        editListener.handle("redo");
    }


    /**
     * Paste the text from the clipboard.
     */
    public void pasteFromClipboard() {
        String text = Clipboards.get();
        if (!text.isEmpty()) {
            input(text);
        }
    }


    /**
     * Copy the selection text to the clipboard.
     */
    public void copyToClipboard() {
        copyToClipboard(false);
    }


    /**
     * Cut the selection text to the clipboard.
     */
    public void cutToClipboard() {
        copyToClipboard(true);
    }


    /**
     * Copy the selection text to the clipboard.
     * @param cut need cut?
     */
    private void copyToClipboard(boolean cut) {
        if (!pointing.selectionOn()) return;

        int[] range = pointing.selectionOffsets();
        int len = range[1] - range[0];
        if (len > 0) {
            String text = model.substring(range[0], len);
            if (text != null && !text.isEmpty()) {
                Clipboards.put(text);
                if (cut) {
                    selectionDelete();
                    textFlow.setAll(Texts.asText(model.text()));
                }
            }
        }
    }


    /**
     * Delete the selecting text.
     */
    public void selectionDelete() {
        if (!pointing.selectionOn()) return;

        pointing.normalizeSelectionCaret();
        caretBehavior.at();
        int[] range = pointing.selectionOffsets();
        model.delete(range[0], range[1] - range[0]);
        pointing.clearSelection();
        editListener.handle("selectionDelete");
    }


    /**
     * Dump.
     */
    public void dump() {
        String buf = model.stringSlice();
        String src = model.substring(0, Math.min(model.tailOffset(), 100));
        if (!buf.equals(src)) {
            log.log(Level.INFO, "buf[" + buf.replaceAll("\n", "$") + "]");
            log.log(Level.INFO, "src[" + src.replaceAll("\n", "$") + "]");
        }
        log.log(Level.INFO, "totalRowSize   [" + model.totalRowSize() + "]");
        log.log(Level.INFO, "originRowIndex [" + model.originRowIndex() + "]");
        log.log(Level.INFO, "originOffset   [" + model.originOffset() + "]");
        log.log(Level.INFO, "tailOffset     [" + model.tailOffset() + "]");
    }

}
