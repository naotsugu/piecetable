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

import com.mammb.code.editor3.ui.control.ColScrollBar;
import com.mammb.code.editor3.ui.control.RowScrollBar;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.layout.Region;

/**
 * Scrolling.
 * @author Naotsugu Kobayashi
 */
public class Scrolling extends Region {

    /** The row scroll bar. */
    private final RowScrollBar rowScroll;

    /** The col scroll bar. */
    private final ColScrollBar colScroll;


    /**
     * Constructor.
     */
    public Scrolling() {

        this.rowScroll = new RowScrollBar();
        this.colScroll = new ColScrollBar();
        getChildren().addAll(rowScroll, colScroll);

        initHandler();
    }


    private void initHandler() {

        // locate the scroll bars at the edge.
        layoutBoundsProperty().addListener(this::boundsChanged);

        // adjust the breadth size to match one display to the other.
        rowScroll.visibleProperty().addListener((os, ov, nv) -> fitScrollBar());
        colScroll.visibleProperty().addListener((os, ov, nv) -> fitScrollBar());

    }

    public void setRowScale(int maxSize, int visibleSize) {
        rowScroll.setScale(maxSize, visibleSize);
    }


    public void setColScale(double maxSize, double visibleSize) {
        colScroll.setScale(maxSize, visibleSize);
    }


    // -- private -------------------------------------------------------------

    private void boundsChanged(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        if (oldValue.getWidth() != newValue.getWidth()) {
            rowScroll.setLayoutX(newValue.getWidth() - (rowScroll.getWidth() + 2));
        }
        if (oldValue.getHeight() != newValue.getHeight()) {
            colScroll.setLayoutY(newValue.getHeight() - (colScroll.getHeight() + 2));
        }
        if (oldValue.getWidth() != newValue.getWidth() || oldValue.getHeight() != newValue.getHeight()) {
            fitScrollBar();
        }
    }


    private void fitScrollBar() {
        rowScroll.setPrefHeight(getHeight() - (colScroll.isVisible() ? colScroll.getHeight() : 0));
        colScroll.setPrefWidth(getWidth() - (rowScroll.isVisible() ? rowScroll.getWidth() : 0));
    }

}
