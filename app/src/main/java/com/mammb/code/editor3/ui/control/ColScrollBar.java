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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.AccessibleRole;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * ColScrollBar.
 * @author Naotsugu Kobayashi
 */
public class ColScrollBar extends StackPane {

    /** The height of scroll bar. */
    private final double HEIGHT = 8;

    /** The thumb. */
    private final ScrollThumb thumb = ScrollThumb.rowOf(HEIGHT);

    /** The min value of scroll bar. */
    private final DoubleProperty min = new SimpleDoubleProperty(0);

    /** The max value of scroll bar. */
    private final DoubleProperty max = new SimpleDoubleProperty(0);

    /** The value of scroll bar. */
    private final DoubleProperty value = new SimpleDoubleProperty(0);


    /**
     * Constructor.
     */
    public ColScrollBar() {
        setManaged(false);
        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setHeight(HEIGHT);
        getChildren().add(thumb);
        parentProperty().addListener(this::parentChanged);
    }


    private void parentChanged(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
        if (oldValue != newValue && newValue instanceof Region region) {
            layoutYProperty().bind(region.heightProperty().subtract(HEIGHT + 2));
            region.widthProperty().addListener((obs, prev, curr) -> setWidth(curr.doubleValue()));
        }
    }


    private double computeThumbWidth() {
        double thumbLength = getWidth();
        return getWidth() * thumbLength / Math.max(max.get() - min.get(), thumbLength);
    }


    private double clamp(double value) {
        return Math.min(Math.max(min.get(), value), max.get());
    }

}
