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

import com.mammb.code.editor.ui.util.Texts;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;

/**
 * Screen bound property collection.
 * <pre>
 *                              0   1   2   3   4   5   6
 *                   0:   1   | a | b | $ |   |   |   |   |
 *                   1:   2   | a | b | c | $ |   |   |   |   __
 *  rowOffset:2 ->   2:   3   | a | b | c | d | e | f | g |    |
 *                        4   | h | i | j | k | l | m | n |   _|  <- lineSlipOffset:2
 *                            ------------------------------  __
 *                        5   | o | p | q | $ |   |   |   |    |
 *                   3:   6   | a | $ |   |   |   |   |   |    |
 *                   4:   7   | a | b | $ |   |   |   |   |    |
 *                   5:   8   | a | b | c | $ |   |   |   |    |
 *                   6:   9   | a | b | c | d | $ |   |   |   _|  <- visibleLineSize:5
 *                            ------------------------------
 *                   7:  10   | a | b | c | d | e | $ |   |
 *                   8:  11   |   |   |   |   |   |   |   |
 *                   |
 *                   |        |___________________________|  <- visibleColSize(pixel)
 *                   |         <- colOffset(pixel)
 *                   |
 *    totalRowSize = 9 + 2(wrapped lines in display area)
 *    totalColSize = pixel of longest Row in display area
 * </pre>
 * @author Naotsugu Kobayashi
 */
public class ScreenBound {

    /** The row offset. */
    private final IntegerProperty rowOffset = new SimpleIntegerProperty(0);
    /** The line slip offset. */
    private final IntegerProperty lineSlipOffset = new SimpleIntegerProperty(0);
    /** The col offset. */
    private final DoubleProperty colOffset = new SimpleDoubleProperty(0);

    /** The visible row size. */
    private final IntegerProperty visibleLineSize = new SimpleIntegerProperty(0);
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
            visibleLineSize.set((int) Math.ceil(nv.getHeight() / Texts.height)));
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
     * @param slipLine the number of slip line
     */
    public void setRowOffset(int value, int slipLine) {
        rowOffset.set(value);
        lineSlipOffset.set(value + slipLine);
    }

    // <editor-fold desc="properties">

    public IntegerProperty rowOffsetProperty() { return rowOffset; }
    public IntegerProperty lineSlipOffsetProperty() { return lineSlipOffset; }
    public IntegerProperty visibleLineSizeProperty() { return visibleLineSize; }
    public IntegerProperty totalRowSizeProperty() { return totalRowSize; }

    public DoubleProperty colOffsetProperty() { return colOffset; }
    public DoubleProperty visibleColSizeProperty() { return visibleColSize; }
    public DoubleProperty totalColSizeProperty() { return totalColSize; }

    // </editor-fold>

}
