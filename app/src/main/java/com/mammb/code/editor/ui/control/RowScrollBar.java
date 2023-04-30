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
package com.mammb.code.editor.ui.control;

import com.mammb.code.editor.ui.util.Colors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;

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
    private final IntegerProperty visibleAmount = new SimpleIntegerProperty(100);


    /**
     * Constructor.
     */
    public RowScrollBar() {
        setManaged(false);
        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(Colors.scrollTrack, null, null)));

        setWidth(WIDTH);
        getChildren().add(thumb);

        initListener();
    }


    /**
     * Initialize listener.
     */
    private void initListener() {
        max.addListener((os, ov, nv) -> thumb.lengthProperty().set(computeThumbHeight()));
        visibleAmount.addListener((os, ov, nv) -> thumb.lengthProperty().set(computeThumbHeight()));
        value.addListener(this::handleValueChanged);
    }


    /**
     * Compute thumb height.
     * @return thumb height
     */
    private double computeThumbHeight() {
        int length = visibleAmount.get();
        double thumbHeight = getHeight() * length / Math.max(max.get() - min.get(), length);
        setVisible(thumbHeight != getHeight());
        return Math.max(thumbHeight, WIDTH);
    }


    /**
     * The value change handler.
     * @param observable the ObservableValue which value changed
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void handleValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double val = clamp(newValue.intValue());
        double y = getHeight() * (val - min.get()) / max.get() - min.get();
        thumb.setY(y);
    }


    /**
     * Set the layout height.
     * @param height the layout height
     */
    public void setLayoutHeight(double height) {
        setHeight(height);
    }


    /**
     * Clamp the value.
     * @param value the value
     * @return the clamped value
     */
    private int clamp(int value) {
        return Math.min(Math.max(min.get(), value), max.get());
    }


    public final IntegerProperty maxProperty() { return max; }
    public final IntegerProperty visibleAmountProperty() { return visibleAmount; }
    public final IntegerProperty valueProperty() { return value; }

}
