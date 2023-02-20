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
package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.ui.util.Texts;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;

/**
 * UiCaret.
 * @author Naotsugu Kobayashi
 */
public class UiCaret extends Path {

    /** The default height of caret. */
    private static double DEFAULT_HEIGHT = Texts.height;

    /** The textFlow. */
    private final TextFlow textFlow;

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
    public UiCaret(TextFlow textFlow) {

        setStrokeWidth(2);
        setStroke(Color.ORANGE);
        setManaged(false);
        layoutXProperty().bind(textFlow.layoutXProperty().add(textFlow.getPadding().getLeft()));
        layoutYProperty().bind(textFlow.layoutYProperty().add(textFlow.getPadding().getTop()));

        this.textFlow = textFlow;

        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> setVisible(!isVisible())));
        timeline.play();

        setShape(defaultShape());
    }


    /**
     * Reset caret.
     */
    public void reset() {
        moveToPoint(0, 0);
        logicalX = physicalX;
    }


    /**
     * Disable this caret.
     */
    void disable() {
        timeline.stop();
        setVisible(false);
        getElements().remove(0, getElements().size());
    }


    public void addOffset(int delta) {
        if (delta == 0) return;
        offset += delta;
        moveToOffset();
    }


    /**
     * Move the caret to the right.
     * @return the caret offset
     */
    public int right() {
        if (Character.isHighSurrogate(textFlow.charAt(++offset))) {
            offset++;
        }
        moveToOffsetSyncLogical();
        return offset;
    }


    /**
     * Move the caret to the left.
     * @return the caret offset
     */
    public int left() {
        if (offset == 0) return 0;
        if (Character.isLowSurrogate(textFlow.charAt(--offset))) {
            offset--;
        }
        moveToOffsetSyncLogical();
        return offset;
    }


    /**
     * Move the caret to the end of row.
     * @return the caret offset
     */
    public int end() {
        moveToPoint(Double.MAX_VALUE, physicalY);
        logicalX = physicalX;
        return offset;
    }


    /**
     * Move the caret to the end of row.
     * @return the caret offset
     */
    public int home() {
        moveToPoint(0, physicalY);
        logicalX = physicalX;
        return offset;
    }


    /**
     * Move the caret down.
     * @return the caret offset
     */
    public int down() { return moveToPoint(logicalX, physicalY + height); }


    /**
     * Move the caret up.
     * @return the caret offset
     */
    public int up() { return moveToPoint(logicalX, physicalY - 1); }


    private boolean moveToOffsetSyncLogical() {
        boolean moved = moveToOffset();
        if (moved) {
            logicalX = physicalX;
        }
        return moved;
    }


    private boolean moveToOffset() {
        double oldX = physicalX;
        double oldY = physicalY;
        setShape(textFlow.caretShape(offset, true));
        return oldX != physicalX || oldY != physicalY;
    }


    private int moveToPoint(double x, double y) {
        offset = textFlow.insertionIndexAt(x, y);
        setShape(textFlow.caretShape(offset, true));
        return offset;
    }


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
        if (offset >= 0 && offset <= textFlow.textLength()) {
            getElements().setAll(elements);
            setVisible(true);
            timeline.play();
        }
    }

    public void slipY(double delta) {
        physicalY += delta;
        getElements().forEach(e -> {
            if (e instanceof MoveTo moveTo) {
                moveTo.setY(moveTo.getY() + delta);
            } else if (e instanceof LineTo lineTo) {
                lineTo.setY(lineTo.getY() + delta);
            }
        });
    }


    /**
     * Get the caret offset.
     * @return the caret offset
     */
    public int offset() { return offset; }

    public double physicalX() { return physicalX; }

    public double physicalY() { return physicalY; }

    public double height() { return height; }

    public double bottom() { return physicalY + height; }

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

}
