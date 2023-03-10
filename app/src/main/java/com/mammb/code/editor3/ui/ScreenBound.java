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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;

/**
 * Screen bound property collection.
 * @author Naotsugu Kobayashi
 */
public class ScreenBound {

    /** The row offset. */
    private final IntegerProperty rowOffset = new SimpleIntegerProperty(0);
    /** The col offset. */
    private final DoubleProperty colOffset = new SimpleDoubleProperty(0);

    /** The visible row size. */
    private final IntegerProperty visibleRowSize = new SimpleIntegerProperty(0);
    /** The visible col size. */
    private final DoubleProperty visibleColSize = new SimpleDoubleProperty(0);

    /** The total row size. */
    private final IntegerProperty totalRowSize = new SimpleIntegerProperty(0);
    /** The total col size. */
    private final DoubleProperty totalColSize = new SimpleDoubleProperty(0);


    /**
     * Constructor.
     * @param textPane the textPane
     * @param textFlow the textFlow
     */
    public ScreenBound(Pane textPane, Pane textFlow) {

        // changes the visible view size according to the bounds of the pane
        textPane.layoutBoundsProperty().addListener((os, ov, nv) ->
            visibleRowSize.set((int) Math.ceil(nv.getHeight() / Texts.height)));
        visibleColSize.bind(textPane.widthProperty());

        // total col size = width of textFlow
        totalColSize.bind(textFlow.widthProperty());

        // colOffset == translateX of textFlow
        colOffset.bindBidirectional(textFlow.translateXProperty());

    }


    /**
     * Set the total row size.
     * @param value the total row size
     */
    public void setTotalRowSize(int value) {
        totalRowSize.set(value);
    }


    /**
     * Set the row offset.
     * @param value the row offset
     */
    public void setRowOffset(int value) {
        rowOffset.set(value);
    }

    // <editor-fold desc="properties">

    public IntegerProperty rowOffsetProperty() { return rowOffset; }
    public IntegerProperty visibleRowSizeProperty() { return visibleRowSize; }
    public IntegerProperty totalRowSizeProperty() { return totalRowSize; }

    public DoubleProperty colOffsetProperty() { return colOffset; }
    public DoubleProperty visibleColSizeProperty() { return visibleColSize; }
    public DoubleProperty totalColSizeProperty() { return totalColSize; }

    // </editor-fold>

}
