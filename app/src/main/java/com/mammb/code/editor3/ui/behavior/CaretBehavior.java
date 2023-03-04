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

import com.mammb.code.editor3.ui.Pointing;
import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * CaretBehavior.
 * @author Naotsugu Kobayashi
 */
public class CaretBehavior {

    /** The pointing. */
    private final Pointing pointing;

    /** The scroll behavior. */
    private final ScrollBehavior scrollBehavior;

    /** The height of view port. */
    private final ReadOnlyDoubleProperty viewHeight;


    /**
     * Constructor.
     * @param pointing the pointing
     * @param scrollBehavior the scroll behavior
     */
    public CaretBehavior(Pointing pointing,
            ScrollBehavior scrollBehavior,
            ReadOnlyDoubleProperty viewHeight) {
        this.pointing = pointing;
        this.scrollBehavior = scrollBehavior;
        this.viewHeight = viewHeight;
    }


    /**
     * Move the caret to the right.
     */
    public void right() {
        scrollToCaretPoint();
        pointing.right();
        if (pointing.caretTop() >= viewHeight.get()) {
            scrollBehavior.scrollNext();
        }
    }


    /**
     * Move the caret to the left.
     */
    public void left() {
        scrollToCaretPoint();
        pointing.left();
        if (pointing.caretTop() < 0) {
            scrollBehavior.scrollPrev();
        }
    }


    /**
     * Move the caret up.
     */
    public void up() {
        scrollToCaretPoint();
        if (pointing.caretTop() <= 0) {
            scrollBehavior.scrollPrev();
        }
        pointing.up();
    }


    /**
     * Move the caret down.
     */
    public void down() {
        scrollToCaretPoint();
        int old = pointing.caretOffset();
        pointing.down();
        if (old == pointing.caretOffset()) return;
        if (pointing.caretBottom() >= viewHeight.get()) {
            scrollBehavior.scrollNext();
        }
    }


    /**
     * Move the caret to the end of row.
     */
    public void end() {
        pointing.end();
    }


    /**
     * Move the caret to the end of row.
     */
    public void home() {
        pointing.home();
    }


    /**
     * Start select.
     */
    public void select() {
        if (!pointing.selectionOn()) {
            pointing.startSelection();
        }
    }


    /**
     * Clear select.
     */
    public void clearSelect() {
        if (pointing.selectionOn()) {
            pointing.clearSelection();
        }
    }


    /**
     * Scroll the screen to the caret position, if the caret is off-screen.
     */
    private void scrollToCaretPoint() {
        while (pointing.caretOffset() < 0)
            scrollBehavior.scrollPrev();
        while (pointing.caretBottom() > viewHeight.get())
            scrollBehavior.scrollNext();
    }

}
