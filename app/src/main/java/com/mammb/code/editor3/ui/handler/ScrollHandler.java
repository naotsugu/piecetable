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
package com.mammb.code.editor3.ui.handler;

import com.mammb.code.editor3.model.behavior.ScrollBehavior;
import com.mammb.code.editor3.ui.TextFlow;
import com.mammb.code.editor3.ui.UiCaret;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;

/**
 * ScrollHandler.
 * @author Naotsugu Kobayashi
 */
public class ScrollHandler implements EventHandler<ScrollEvent> {

    /** The text flow pane. */
    private final TextFlow textFlow;

    /** The ui caret. */
    private final UiCaret caret;

    /** The scroll behavior. */
    private final ScrollBehavior scrollBehavior;


    /**
     * Constructor.
     * @param textFlow the text flow pane
     * @param caret the ui caret
     * @param scrollBehavior the scroll behavior
     */
    private ScrollHandler(TextFlow textFlow, UiCaret caret, ScrollBehavior scrollBehavior) {
        this.textFlow = textFlow;
        this.caret = caret;
        this.scrollBehavior = scrollBehavior;
    }


    /**
     * Create a new {@code EventHandler<ScrollEvent>}.
     * @param textFlow the text flow pane
     * @param caret the ui caret
     * @param scrollBehavior the scroll behavior
     * @return a new {@code EventHandler<ScrollEvent>}
     */
    public static EventHandler<ScrollEvent> of(TextFlow textFlow, UiCaret caret, ScrollBehavior scrollBehavior) {
        return new ScrollHandler(textFlow, caret, scrollBehavior);
    }


    @Override
    public void handle(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL) {
            if (e.getDeltaY() > 0) {
                scrollPrev();
            } else if (e.getDeltaY() < 0) {
                scrollNext();
            }
        }
    }


    private void scrollNext() {
        if (textFlow.canTranslateRowNext()) {
            double old = textFlow.getTranslateY();
            textFlow.translateRowNext();
            caret.slipY(textFlow.getTranslateY() - old);
        } else {
            int shiftedOffset = scrollBehavior.next(1);
            caret.addOffset(shiftedOffset);
        }
    }


    private void scrollPrev() {
        if (textFlow.canTranslateRowPrev()) {
            double old = textFlow.getTranslateY();
            textFlow.translateRowPrev();
            caret.slipY(textFlow.getTranslateY() - old);
        } else {
            int shiftedOffset = scrollBehavior.prev(1);
            caret.addOffset(shiftedOffset);
        }
    }

}
