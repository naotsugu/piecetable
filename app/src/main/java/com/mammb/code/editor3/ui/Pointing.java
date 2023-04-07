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

import javafx.geometry.Point2D;
import javafx.scene.layout.Region;

/**
 * The layer of caret and selection.
 * @author Naotsugu Kobayashi
 */
public class Pointing extends Region {

    /** The caret. */
    private final Caret caret;

    /** The selection. */
    private final Selection selection;


    /**
     * Constructor.
     * @param textFlow the TextFlow
     */
    public Pointing(TextFlow textFlow) {

        this.caret = new Caret(textFlow);
        this.selection = new Selection(textFlow);

        setManaged(false);
        layoutXProperty().bind(textFlow.layoutXProperty().add(textFlow.getPadding().getLeft()));
        layoutYProperty().bind(textFlow.layoutYProperty().add(textFlow.getPadding().getTop()).subtract(1));

        translateYProperty().bind(textFlow.translateYProperty());
        translateXProperty().bindBidirectional(textFlow.translateXProperty());

        getChildren().setAll(selection, caret);
    }


    /**
     * Clear pointing.
     */
    public void clear() {
        caret.clear();
        selection.clear();
    }


    /**
     * Shift this pointing.
     * @param delta the shift of delta
     */
    public void addOffset(int delta) {
        caret.shiftOffset(delta);
        selection.shiftOffset(delta);
    }


    /**
     * Move the caret to the right.
     */
    public void right() {
        caret.right();
        selection.moveCaretTo(caret.offset());
    }


    /**
     * Move the caret to the left.
     */
    public void left() {
        caret.left();
        selection.moveCaretTo(caret.offset());
    }


    /**
     * Move the caret to the end of row.
     */
    public void end() {
        caret.end();
        selection.moveCaretTo(caret.offset());
    }


    /**
     * Move the caret to the end of row.
     */
    public void home() {
        caret.home();
        selection.moveCaretTo(caret.offset());
    }


    /**
     * Move the caret down.
     */
    public void down() {
        caret.down();
        selection.moveCaretTo(caret.offset());
    }


    /**
     * Move the caret up.
     */
    public void up() {
        caret.up();
        selection.moveCaretTo(caret.offset());
    }


    /**
     * Move the caret to the specified point.
     * @param x point of x on scene
     * @param y point of y on scene
     */
    public void caretAt(double x, double y) {
        Point2D p = sceneToLocal(new Point2D(x, y));
        caret.at(p.getX(), p.getY());
    }


    /**
     * Move the caret to the specified point.
     * @param y point of y on local
     */
    public void caretRawAt(double y) {
        caret.moveToPointRow(y);
    }


    /**
     * Move the caret to the specified point on dragged.
     * @param x point of x on local
     * @param y point of y on local
     */
    public void dragged(double x, double y) {
        Point2D p = sceneToLocal(new Point2D(x, y));
        caret.at(p.getX(), p.getY());
        if (selection.isDragging()) {
            selection.moveCaretTo(caret.offset());
        } else {
            selection.startDragging(caret.offset());
        }
    }


    /**
     * Select text around the caret.
     */
    public void selectAround() {
        selection.selectAround(caret.offset());
        if (selectionOn()) {
            caret.shiftOffset(selection.closeOffset() - caret.offset());
        }
    }


    /**
     * Hide caret.
     */
    public void hideCaret() {
        caret.stop();
    }


    /**
     * Show caret.
     */
    public void showCaret() {
        caret.start();
    }


    /**
     * Get the caret offset.
     * @return the caret offset
     */
    public int caretOffset() {
        return caret.offset();
    }


    /**
     * Get the caret top position.
     * @return the caret top position
     */
    public double caretTop() {
        return getTranslateY() + caret.physicalYInParent();
    }


    /**
     * Get the caret bottom position
     * @return the caret bottom position
     */
    public double caretBottom() {
        return getTranslateY() + caret.physicalYInParent() + caret.height();
    }


    /**
     * Get the caret x position.
     * @return the caret x position
     */
    public double caretX() {
        return getTranslateX() + caret.physicalX();
    }


    /**
     * Start selection.
     */
    public void startSelection() {
        selection.start(caret.offset());
    }


    /**
     * Get the selection offsets.
     * @return the selection offsets
     */
    public int[] selectionOffsets() {
        int start = selection.openOffset();
        int end   = selection.closeOffset();
        return new int[] {
            Math.min(start, end),
            Math.max(start, end) };
    }


    /**
     * Normalize selection caret.
     * Shift caret offset to the selection start position.
     */
    public void normalizeSelectionCaret() {
        if (selectionOn()) {
            int selectionHeadOffset = Math.min(
                selection.openOffset(),
                selection.closeOffset());
            caret.shiftOffset(selectionHeadOffset - caret.offset());
        }
    }


    /**
     * Clear this selection.
     */
    public void clearSelection() {
        if (selection.isDragging()) {
            selection.releaseDragging();
        } else {
            selection.clear();
        }
    }


    /**
     * Get the selection on.
     * @return the selection on
     */
    public boolean selectionOn() {
        return selection.on();
    }


    /**
     * Slip caret.
     * @param translateX slip x
     */
    public void slipCaret(double translateX) {
        caret.setTranslateX(translateX);
    }

}
