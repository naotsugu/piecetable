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
package com.mammb.code.editor3.ui.control;

import com.mammb.code.editor.ScrollBar;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * RowScrollBar.
 * @author Naotsugu Kobayashi
 */
public class RowScrollBar extends StackPane {

    /** The width of scroll bar. */
    private final double WIDTH = 8;

    /** The thumb. */
    private final ScrollThumb thumb = ScrollThumb.rowOf(WIDTH);

    /** The min value of scroll bar. */
    private final IntegerProperty min = new SimpleIntegerProperty(0);

    /** The max value of scroll bar. */
    private final IntegerProperty max = new SimpleIntegerProperty(100);

    /** The value of scroll bar. */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /** The visible amount. */
    private final IntegerProperty visibleAmount = new SimpleIntegerProperty(10);

    /** The breadth height property. */
    private final DoubleProperty breadthHeight = new SimpleDoubleProperty(WIDTH);



    /**
     * Constructor.
     */
    public RowScrollBar() {
        setManaged(false);
        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setWidth(WIDTH);
        getChildren().add(thumb);
        parentProperty().addListener(this::parentChanged);
    }


    private void parentChanged(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
        if (oldValue != newValue && newValue instanceof Region region) {
            layoutXProperty().bind(region.widthProperty().subtract(WIDTH + 2));
            region.heightProperty().addListener((obs, prev, curr) ->
                setHeight(curr.doubleValue() - breadthHeight.get()));
        }
    }


    private double computeThumbHeight() {
        double thumbLength = visibleAmount.get();
        return getHeight() * thumbLength / Math.max(max.get() - min.get(), thumbLength);
    }


    private int clamp(int value) {
        return Math.min(Math.max(min.get(), value), max.get());
    }

}
