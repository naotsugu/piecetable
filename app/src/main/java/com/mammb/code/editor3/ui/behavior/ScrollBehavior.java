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

import com.mammb.code.editor3.model.TextModel;
import com.mammb.code.editor3.ui.RowsPanel;
import com.mammb.code.editor3.ui.TextFlow;
import com.mammb.code.editor3.ui.UiCaret;
import com.mammb.code.editor3.ui.util.Texts;

/**
 * ScrollBehavior.
 * @author Naotsugu Kobayashi
 */
public class ScrollBehavior {

    /** The text model. */
    private final TextModel model;

    /** The text flow pane. */
    private final TextFlow textFlow;

    /** The ui caret. */
    private final UiCaret caret;

    /** The rows panel. */
    private final RowsPanel rowsPanel;


    /**
     * Constructor.
     * @param textFlow the text flow pane
     * @param caret the ui caret
     * @param model the text model
     * @param rowsPanel the rows panel
     */
    public ScrollBehavior(TextFlow textFlow, UiCaret caret, TextModel model, RowsPanel rowsPanel) {
        this.textFlow = textFlow;
        this.caret = caret;
        this.model = model;
        this.rowsPanel = rowsPanel;
    }


    /**
     * Scroll next (i.e. arrow down).
     */
    public void scrollNext() {
        if (textFlow.canTranslateRowNext()) {
            // slip scroll
            double old = textFlow.getTranslateY();
            textFlow.translateRowNext();
            caret.slipY(textFlow.getTranslateY() - old);
        } else {
            int shiftedOffset = model.scrollNext(textFlow.translatedShiftRow() + 1);
            if (shiftedOffset > 0) {
                textFlow.clearTranslation();
                caret.addOffset(-shiftedOffset);
                textFlow.setAll(Texts.asText(model.text()));
                rowsPanel.draw(model.originRowIndex());
            }
        }
    }


    /**
     * Scroll prev (i.e. arrow up).
     */
    public void scrollPrev() {
        if (textFlow.canTranslateRowPrev()) {
            // slip scroll
            double old = textFlow.getTranslateY();
            textFlow.translateRowPrev();
            caret.slipY(textFlow.getTranslateY() - old);
        } else {
            int shiftedOffset = model.scrollPrev(textFlow.translatedShiftRow() + 1);
            if (shiftedOffset > 0) {
                caret.addOffset(shiftedOffset);
                textFlow.setAll(Texts.asText(model.text()));
                rowsPanel.draw(model.originRowIndex());
            }
        }
    }

}
