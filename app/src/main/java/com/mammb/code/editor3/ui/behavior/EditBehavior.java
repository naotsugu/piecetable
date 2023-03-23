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
package com.mammb.code.editor3.ui.behavior;

import com.mammb.code.editor3.lang.Strings;
import com.mammb.code.editor3.model.TextModel;
import com.mammb.code.editor3.ui.Pointing;
import com.mammb.code.editor3.ui.RowsPanel;
import com.mammb.code.editor3.ui.TextFlow;
import com.mammb.code.editor3.ui.util.Clipboards;
import com.mammb.code.editor3.ui.util.Texts;

/**
 * InputBehavior.
 * @author Naotsugu Kobayashi
 */
public class EditBehavior {

    /** The text model. */
    private final TextModel model;

    /** The pointing. */
    private final Pointing pointing;

    /** The text flow pane. */
    private final TextFlow textFlow;

    /** The rows panel. */
    private final RowsPanel rowsPanel;

    /** The caret behavior. */
    private final CaretBehavior caretBehavior;


    /**
     * Constructor.
     * @param model the text model
     * @param pointing the pointing
     * @param textFlow the text flow pane
     * @param rowsPanel the rows panel
     * @param caretBehavior the caret behavior
     */
    public EditBehavior(TextModel model, Pointing pointing,
            TextFlow textFlow, RowsPanel rowsPanel,
            CaretBehavior caretBehavior) {
        this.model = model;
        this.pointing = pointing;
        this.textFlow = textFlow;
        this.rowsPanel = rowsPanel;
        this.caretBehavior = caretBehavior;
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
            value = value.replace('\r', '\n');
        }
        caretBehavior.at();
        model.add(pointing.caretOffset(), value);
        textFlow.setAll(Texts.asText(model.text()));
        for (int i = 0; i < Strings.codePointCount(value); i++)
            caretBehavior.right();
        rowsPanel.redraw();
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
        rowsPanel.redraw();
    }


    /**
     * Delete text with backspace.
     */
    public void backspace() {
        if (pointing.selectionOn()) {
            selectionDelete();
        } else {
            caretBehavior.at();
            caretBehavior.left();
            model.delete(pointing.caretOffset(), 1);
        }
        textFlow.setAll(Texts.asText(model.text()));
        rowsPanel.redraw();
    }


    public void undo() {
        pointing.clearSelection();
        model.undo();
        textFlow.setAll(Texts.asText(model.text()));
        rowsPanel.redraw();
    }


    public void redo() {
        pointing.clearSelection();
        model.redo();
        textFlow.setAll(Texts.asText(model.text()));
        rowsPanel.redraw();
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
                    rowsPanel.redraw();
                }
            }
        }
    }


    /**
     * Delete the selecting text.
     */
    private void selectionDelete() {
        if (!pointing.selectionOn()) return;

        pointing.normalizeSelectionCaret();
        caretBehavior.at();
        int[] range = pointing.selectionOffsets();
        model.delete(range[0], range[1] - range[0]);
        pointing.clearSelection();
    }

}
