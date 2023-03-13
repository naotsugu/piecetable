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
import com.mammb.code.editor3.ui.Pointing;
import com.mammb.code.editor3.ui.TextFlow;
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

    /** The caret behavior. */
    private final CaretBehavior caretBehavior;


    /**
     * Constructor.
     * @param model the text model
     * @param pointing the pointing
     * @param textFlow the text flow pane
     * @param caretBehavior the caret behavior
     */
    public EditBehavior(TextModel model, Pointing pointing,
            TextFlow textFlow, CaretBehavior caretBehavior) {
        this.model = model;
        this.pointing = pointing;
        this.textFlow = textFlow;
        this.caretBehavior = caretBehavior;
    }


    public void input(String value) {
        if (pointing.selectionOn()) {
            selectionDelete();
        }
        if (value.contains("\r")) {
            // Enter key : 13:CR -> replace to 10:LF
            value = value.replace('\r', '\n');
        }
        model.add(pointing.caretOffset(), value);
        textFlow.setAll(Texts.asText(model.text()));
        caretBehavior.right();
    }


    public void delete() {
        if (pointing.selectionOn()) {
            selectionDelete();
        } else {
            model.delete(pointing.caretOffset(), 1);
        }
        textFlow.setAll(Texts.asText(model.text()));
    }


    public void backspace() {
        if (pointing.selectionOn()) {
            selectionDelete();
        } else {
            caretBehavior.right();
            model.delete(pointing.caretOffset(), 1);
        }
        textFlow.setAll(Texts.asText(model.text()));
    }


    private void selectionDelete() {
        int[] range = pointing.selectionOffsets();
        model.delete(range[0], range[1] - range[0]);
        pointing.clearSelection();
    }

}
