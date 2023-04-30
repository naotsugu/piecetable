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
package com.mammb.code.editor.ui;

import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;

/**
 * Selection.
 * @author Naotsugu Kobayashi
 */
public class Selection extends Path {

    /** The ScreenText. */
    private final ScreenText text;

    /** The selection open offset. */
    private int openOffset = -1;

    /** The selection close offset. */
    private int closeOffset = -1;

    /** The selection on. */
    private boolean on = false;

    /** The selection dragging. */
    private boolean dragging = false;


    /**
     * Constructor.
     * @param text the ScreenText
     */
    public Selection(ScreenText text) {

        this.text = text;

        setStrokeWidth(0);
        setOpacity(0.3);
        setFill(Color.DEEPSKYBLUE);
        setBlendMode(BlendMode.LIGHTEN);
        setManaged(false);

    }


    /**
     * Start selection.
     * @param offset the start position
     */
    public void start(int offset) {
        openOffset = closeOffset = offset;
        on = true;
        dragging = false;
    }


    /**
     * Move the selection caret.
     * @param newOffset the new offset
     */
    public void moveCaretTo(int newOffset) {
        if (on) {
            closeOffset = newOffset;
            drawSelection();
        }
    }


    /**
     * Clear this selection.
     */
    public void clear() {
        openOffset = -1;
        closeOffset = -1;
        on = false;
        dragging = false;
        getElements().clear();
    }


    /**
     * Select text around the caret.
     * @param offset the base offset
     */
    public void selectAround(int offset) {

        int type = Character.getType(Character.toLowerCase(text.charAt(offset)));
        int start = offset;
        int end = offset;

        for (int i = offset; i >= 0; i--) {
            if (type != Character.getType(Character.toLowerCase(text.charAt(i)))) {
                break;
            } else {
                start = i;
            }
        }
        for (int i = offset; i < text.textLength(); i++) {
            if (type != Character.getType(Character.toLowerCase(text.charAt(i)))) {
                end = i;
                break;
            }
        }
        if (start != end) {
            start(start);
            moveCaretTo(end);
        }
    }


    /**
     * Start drag selection.
     * @param offset the start position
     */
    public void startDragging(int offset) {
        start(offset);
        dragging = true;
    }


    /**
     * Release dragging.
     */
    public void releaseDragging() {
        dragging = false;
    }


    /**
     * Get whether the selection is dragging.
     * @return {@code true}, if the selection is dragging
     */
    public boolean isDragging() {
        return on && dragging;
    }


    /**
     * Shift the current offset.
     * @param delta the number of offsets to shift
     */
    public void shiftOffset(int delta) {
        if (on) {
            openOffset += delta;
            closeOffset += delta;
            drawSelection();
        }
    }


    /**
     * Get the selection open offset.
     * @return the selection open offset
     */
    public int openOffset() { return openOffset; }


    /**
     * The selection close offset.
     * @return the selection close offset
     */
    public int closeOffset() { return closeOffset; }


    /**
     * Get the selection on.
     * @return the selection on
     */
    public boolean on() { return on; }


    /**
     * Draw selection.
     */
    private void drawSelection() {
        if (on) {
            int start = Math.min(openOffset, closeOffset);
            int end   = Math.max(openOffset, closeOffset);
            getElements().setAll(text.rangeShape(start, end));
        } else {
            getElements().clear();
        }
    }

}
