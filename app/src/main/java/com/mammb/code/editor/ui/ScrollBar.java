/*
 * Copyright 2022-2024 the original author or authors.
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

import com.mammb.code.editor.lang.EventListener;
import com.mammb.code.editor.ui.control.ColScrollBar;
import com.mammb.code.editor.ui.control.RowScrollBar;
import com.mammb.code.editor.ui.control.ScrollBarChange;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.layout.Region;

/**
 * ScrollBar.
 * @author Naotsugu Kobayashi
 */
public class ScrollBar extends Region {

    /** The row scroll bar. */
    private final RowScrollBar rowScroll;

    /** The col scroll bar. */
    private final ColScrollBar colScroll;


    /**
     * Constructor.
     */
    public ScrollBar(ScreenBound screenBound) {
        this.rowScroll = new RowScrollBar();
        this.colScroll = new ColScrollBar();
        getChildren().addAll(rowScroll, colScroll);
        initListener(screenBound);
    }


    /**
     * Initialize listener.
     */
    private void initListener(ScreenBound screenBound) {

        // locate the scroll bars at the edge.
        layoutBoundsProperty().addListener(this::boundsChanged);

        // adjust the breadth size to match one display to the other.
        rowScroll.visibleProperty().addListener((os, ov, nv) -> adjustScrollBar());
        colScroll.visibleProperty().addListener((os, ov, nv) -> adjustScrollBar());

        // bind col scroll
        colScroll.maxProperty().bind(screenBound.totalColSizeProperty());
        colScroll.valueProperty().bind(screenBound.colOffsetProperty());
        colScroll.visibleAmountProperty().bind(screenBound.visibleColSizeProperty());

        // bind row scroll
        rowScroll.maxProperty().bind(screenBound.totalRowSizeProperty());
        rowScroll.valueProperty().bind(screenBound.lineSlipOffsetProperty());
        rowScroll.visibleAmountProperty().bind(screenBound.visibleLineSizeProperty());

    }


    /**
     * Set the handler.
     * @param handler the handler
     */
    public void setHandler(EventListener<ScrollBarChange> handler) {
        rowScroll.setHandler(handler);
        colScroll.setHandler(handler);
    }

    // -- private -------------------------------------------------------------

    /**
     * The handler of bounds changed.
     * @param observable the ObservableValue which value changed
     * @param oldValue the old value newValue
     * @param newValue the new value
     */
    private void boundsChanged(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        if (oldValue.getWidth() != newValue.getWidth()) {
            rowScroll.setLayoutX(newValue.getWidth() - (rowScroll.getWidth() + 2));
        }
        if (oldValue.getHeight() != newValue.getHeight()) {
            colScroll.setLayoutY(newValue.getHeight() - (colScroll.getHeight() + 2));
        }
        if (oldValue.getWidth() != newValue.getWidth() || oldValue.getHeight() != newValue.getHeight()) {
            adjustScrollBar();
        }
    }


    /**
     * Adjust the length of the scroll bar to the screen size.
     */
    private void adjustScrollBar() {
        rowScroll.setLayoutHeight(
            getHeight() - (colScroll.isVisible() ? colScroll.getHeight() : 0));
        colScroll.setLayoutWidth(
            getWidth() - (rowScroll.isVisible() ? rowScroll.getWidth() : 0));
    }

}
