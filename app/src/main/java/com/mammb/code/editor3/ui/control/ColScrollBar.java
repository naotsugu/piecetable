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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.AccessibleRole;
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
    private final DoubleProperty max = new SimpleDoubleProperty(100);

    /** The value of scroll bar. */
    private final DoubleProperty value = new SimpleDoubleProperty(0);

    /** The visible amount. */
    private final DoubleProperty visibleAmount = new SimpleDoubleProperty(100);


    /**
     * Constructor.
     */
    public ColScrollBar() {
        setManaged(false);
        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setHeight(HEIGHT);
        getChildren().add(thumb);
    }


    /**
     * Set scroll bar scale.
     * @param maxSize the max value of scroll bar
     * @param visibleSize the visible amount
     */
    public void setScale(double maxSize, double visibleSize) {
        min.set(0);
        max.set(maxSize);
        visibleAmount.set(visibleSize);
    }


    private double computeThumbWidth() {
        double length = visibleAmount.get();
        return getWidth() * length / Math.max(max.get() - min.get(), length);
    }


    private double clamp(double value) {
        return Math.min(Math.max(min.get(), value), max.get());
    }

}
