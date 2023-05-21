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

import com.mammb.code.editor.lang.LineEnding;
import com.mammb.code.editor.ui.util.PathElements;
import com.mammb.code.editor.ui.util.Texts;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;

/**
 * Caret.
 * @author Naotsugu Kobayashi
 */
public class Caret extends Path {

    /** The default height of caret. */
    private static final double DEFAULT_HEIGHT = Texts.height;

    /** The screenText. */
    private final ScreenText text;

    /** The timeline. */
    private final Timeline timeline = new Timeline();

    /** The physical caret position x. */
    private double physicalX;

    /** The physical caret position y. */
    private double physicalY;

    /** The height of caret. */
    private double height;

    /** The logical caret position x. */
    private double logicalX;

    /** The caret offset (textFlow based). */
    private int offset;


    /**
     * Constructor.
     */
    public Caret(ScreenText text) {

        setStrokeWidth(2);
        setStroke(Color.ORANGE);
        setManaged(false);

        this.text = text;

        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(500),
            e -> setVisible(!isVisible())));
        timeline.play();

        setShape(defaultShape());
    }


    /**
     * Reset caret.
     */
    public void clear() {
        offset = 0;
        moveToOffset();
        syncLogicalToPhysical();
    }


    /**
     * Show caret and start blink.
     */
    public void start() {
        setVisible(true);
        timeline.play();
    }


    /**
     * Hide caret and stop blink.
     */
    public void stop() {
        timeline.stop();
        setVisible(false);
    }


    /**
     * Shift caret offset.
     * @param delta the shifting delta
     */
    public void shiftOffset(int delta) {
        if (delta == 0) return;
        offset += delta;
        moveToOffset();
    }


    /**
     * Move the caret to the right.
     */
    public void right() {
        if (text.textLength() > offset) {
            ++offset;
        }
        if (text.textLength() > offset) {
            char ch = text.charAt(offset);
            if (LineEnding.CR.match(ch) || Character.isHighSurrogate(ch)) {
                offset++;
            }
        }
        moveToOffsetSyncLogical();
    }


    /**
     * Move the caret to the left.
     */
    public void left() {
        if (offset == 0) return;
        char ch = text.charAt(--offset);
        if (LineEnding.CR.match(ch) || Character.isLowSurrogate(ch)) {
            offset--;
        }
        moveToOffsetSyncLogical();
    }


    /**
     * Move the caret to the end of row.
     */
    public void end() {
        moveToPoint(Double.MAX_VALUE, physicalY);
        syncLogicalToPhysical();
    }


    /**
     * Move the caret to the end of row.
     */
    public void home() {
        if (physicalX == 0) {
            if (Character.isWhitespace(text.charAt(offset))) {
                while (offset < text.textLength() &&
                    Character.isWhitespace(text.charAt(offset))) {
                    offset++;
                }
                moveToOffset();
                syncLogicalToPhysical();
            }
        } else {
            moveToPoint(0, physicalY);
            syncLogicalToPhysical();
        }
    }


    /**
     * Move the caret down.
     */
    public void down() { moveToPointRow(physicalY + height); }


    /**
     * Move the caret up.
     */
    public void up() { moveToPointRow(physicalY - 1); }


    /**
     * Move the caret to the tail of text.
     */
    public void tail() {
        offset = text.textLength();
        moveToOffset();
        syncLogicalToPhysical();
    }


    /**
     * Move the caret to the specified point.
     * @param x point of x
     * @param y point of y
     */
    public void at(double x, double y) {
        moveToPoint(x, y);
        syncLogicalToPhysical();
    }


    /**
     * sync the logical position x to the physical position.
     */
    public void syncLogicalToPhysical() {
        logicalX = physicalX;
    }


    /**
     * Get the physical y position including TranslateY.
     * @return the physical y position
     */
    public double physicalYInParent() {
        return physicalY + getTranslateY();
    }


    /**
     * Get the text flow based caret offset.
     * @return the text flow based caret offset
     */
    public int offset() { return offset; }


    /**
     * Get the physical caret position x.
     * @return the physical caret position x
     */
    public double physicalX() { return physicalX; }


    /**
     * Get the height of caret.
     * @return the height of caret
     */
    public double height() { return height; }

    // -- private -------------------------------------------------------------

    /**
     * Set the caret shape.
     * @param elements the path elements of caret shape
     */
    private void setShape(PathElement... elements) {

        if (elements == null || elements.length == 0) {
            elements = defaultShape();
        }

        if (elements[0] instanceof MoveTo moveTo) {
            physicalX = moveTo.getX();
            physicalY = moveTo.getY();
        }
        if (elements[1] instanceof LineTo lineTo) {
            if (lineTo.getY() - physicalY == 0) {
                lineTo.setY(physicalY + DEFAULT_HEIGHT);
            }
            height = lineTo.getY() - physicalY;
        }

        timeline.stop();
        setVisible(false);
        if (offset >= 0 && offset <= text.textLength()) {
            getElements().setAll(elements);
            setVisible(true);
            timeline.play();
        }
    }


    /**
     * Get the default caret shape.
     * @return the default caret shape
     */
    private PathElement[] defaultShape() {
        PathElement[] result = new PathElement[2];
        result[0] = new MoveTo(0, 0);
        result[1] = new LineTo(0, DEFAULT_HEIGHT);
        return result;
    }


    /**
     * Move caret to the position of offset and sync logical x.
     * @return {@code true}, if caret is moved
     */
    private boolean moveToOffsetSyncLogical() {
        boolean moved = moveToOffset();
        if (moved) {
            logicalX = physicalX;
        }
        return moved;
    }


    /**
     * Move caret to the position of offset.
     * @return {@code true}, if caret is moved
     */
    public boolean moveToOffset() {
        double oldX = physicalX;
        double oldY = physicalY;
        setShape(text.caretShape(offset, true));
        return oldX != physicalX || oldY != physicalY;
    }


    public int moveToPoint(double x, double y) {
        offset = text.insertionIndexAt(x, y);
        setShape(text.caretShape(offset, true));
        return offset;
    }


    public int moveToPointRow(double y) {
        int indexAt = text.insertionIndexAt(logicalX, y);
        PathElement[] pathElements = text.caretShape(indexAt, true);
        if (PathElements.getY(pathElements[0]) != physicalY) {
            offset = indexAt;
            setShape(pathElements);
        }
        return offset;
    }

}
