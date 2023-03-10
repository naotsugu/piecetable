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
import com.mammb.code.editor3.ui.util.Texts;
import javafx.beans.property.ReadOnlyDoubleProperty;

import java.util.Objects;

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

    /** The width of view port. */
    private final ReadOnlyDoubleProperty viewWidth;


    /**
     * Constructor.
     * @param pointing the pointing
     * @param scrollBehavior the scroll behavior
     * @param viewHeight the height of view port
     * @param viewWidth the width of view port
     */
    public CaretBehavior(Pointing pointing,
            ScrollBehavior scrollBehavior,
            ReadOnlyDoubleProperty viewHeight,
            ReadOnlyDoubleProperty viewWidth) {
        this.pointing = Objects.requireNonNull(pointing);
        this.scrollBehavior = Objects.requireNonNull(scrollBehavior);
        this.viewHeight = Objects.requireNonNull(viewHeight);
        this.viewWidth = Objects.requireNonNull(viewWidth);
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
        scrollColToCaretPoint();
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
        scrollColToCaretPoint();
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
        scrollColToCaretPoint();
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
        scrollColToCaretPoint();
    }


    /**
     * Move the caret to the end of row.
     */
    public void end() {
        pointing.end();
        scrollColToCaretPoint();
    }


    /**
     * Move the caret to the end of row.
     */
    public void home() {
        pointing.home();
        scrollColToCaretPoint();
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


    /**
     * Scroll horizontally to display the caret.
     */
    private void scrollColToCaretPoint() {
        if (pointing.caretX() > (viewWidth.get() - Texts.width * 2)) {
            double delta = pointing.caretX() - (viewWidth.get() - Texts.width * 2);
            pointing.setTranslateX(pointing.getTranslateX() - delta);
        } else if (pointing.caretX() < 0) {
            pointing.setTranslateX(pointing.getTranslateX() - pointing.caretX());
        }
    }

}
