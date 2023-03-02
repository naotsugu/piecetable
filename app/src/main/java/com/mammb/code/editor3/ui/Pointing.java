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

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Pointing.
 * @author Naotsugu Kobayashi
 */
public class Pointing extends Region {

    /** The caret. */
    private final UiCaret caret;

    /** The selection. */
    private final Selection selection;


    /**
     * Constructor.
     * @param textFlow the TextFlow
     */
    public Pointing(TextFlow textFlow) {

        this.caret = new UiCaret(textFlow);
        this.selection = new Selection(textFlow);

        setManaged(false);
        translateXProperty().bind(textFlow.translateXProperty());
        translateYProperty().bind(textFlow.translateYProperty());

        layoutXProperty().bind(textFlow.layoutXProperty().add(textFlow.getPadding().getLeft()));
        layoutYProperty().bind(textFlow.layoutYProperty().add(textFlow.getPadding().getTop()));

        getChildren().setAll(selection, caret);
    }

    /**
     * Reset caret.
     */
    public void reset() {
        caret.reset();
        selection.reset();
    }


    public void addOffset(int delta) {
        caret.addOffset(delta);
    }

    /**
     * Move the caret to the right.
     */
    public void right() { caret.right(); }


    /**
     * Move the caret to the left.
     */
    public void left() { caret.left(); }


    /**
     * Move the caret to the end of row.
     */
    public void end() { caret.end(); }


    /**
     * Move the caret to the end of row.
     */
    public void home() { caret.home(); }


    /**
     * Move the caret down.
     */
    public void down() { caret.down(); }


    /**
     * Move the caret up.
     */
    public void up() { caret.up(); }


    public void hideCaret() {
        caret.stop();
    }

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

    public double caretTop() {
        return getTranslateY() + caret.physicalYInParent();
    }

    public double caretBottom() {
        return getTranslateY() + caret.physicalYInParent() + caret.height();
    }

}
