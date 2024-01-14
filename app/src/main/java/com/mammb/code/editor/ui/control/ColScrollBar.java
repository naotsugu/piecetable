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
package com.mammb.code.editor.ui.control;

import com.mammb.code.editor.lang.EventListener;
import com.mammb.code.editor.ui.util.Colors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.AccessibleRole;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;

/**
 * ColScrollBar.
 * @author Naotsugu Kobayashi
 */
public class ColScrollBar extends StackPane {

    /** The height of scroll bar. */
    private final double HEIGHT = 8;

    /** The thumb. */
    private final ScrollThumb thumb = ScrollThumb.colOf(HEIGHT);

    /** The min value of scroll bar. */
    private final DoubleProperty min = new SimpleDoubleProperty(0);

    /** The max value of scroll bar. */
    private final DoubleProperty max = new SimpleDoubleProperty(100);

    /** The value of scroll bar. */
    private final DoubleProperty value = new SimpleDoubleProperty(0);

    /** The visible amount. */
    private final DoubleProperty visibleAmount = new SimpleDoubleProperty(100);

    /** The handler. */
    private EventListener<ScrollBarChange> handler;


    /**
     * Constructor.
     */
    public ColScrollBar() {

        setManaged(false);
        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(Colors.scrollTrack, null, null)));
        setHeight(HEIGHT);
        getChildren().add(thumb);

        initListener();
    }


    /**
     * Initialize listener.
     */
    private void initListener() {

        max.addListener((os, ov, nv) -> thumb.lengthProperty().set(computeThumbWidth()));
        visibleAmount.addListener((os, ov, nv) -> thumb.lengthProperty().set(computeThumbWidth()));
        value.addListener(this::handleValueChanged);

        setOnMouseEntered(e -> thumb.activeProperty().set(true));
        setOnMouseExited(e  -> thumb.activeProperty().set(thumb.dragProperty().get()));

        thumb.dragPointProperty().addListener(this::handleThumbDragged);
        setOnMouseClicked(this::handleTruckClicked);

    }


    /**
     * The thumb dragged handler.
     * @param observable the ObservableValue which value changed
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void handleThumbDragged(
        ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

        if (!thumb.dragProperty().get() || handler == null) {
            return;
        }
        double delta =  oldValue.doubleValue() - newValue.doubleValue();
        handler.handle(ScrollBarChange.colThumbMovedOf(delta));
    }


    /**
     * The truck clicked handler.
     * @param event the MouseEvent
     */
    private void handleTruckClicked(MouseEvent event) {
        if (event.isSynthesized()) {
            event.consume();
            return;
        }
        if (event.getX() >= thumb.getX() &&
            event.getX() <= thumb.getX() + thumb.getWidth()) {
            event.consume();
            return;
        }

        handler.handle(ScrollBarChange.colTruckClickedOf(
            -Math.signum(event.getX() - thumb.getX()) * visibleAmount.get()));
        event.consume();
    }


    /**
     * Compute thumb width.
     * @return thumb width
     */
    private double computeThumbWidth() {
        double length = visibleAmount.get();
        double thumbWidth = getWidth() * length / Math.max(max.get() - min.get(), length);
        setVisible(thumbWidth != getWidth());
        return Math.max(thumbWidth, HEIGHT);
    }


    /**
     * The value change handler.
     * @param observable the ObservableValue which value changed
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void handleValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double val = clamp(-newValue.doubleValue());
        double x = visibleAmount.get() * (val - min.get()) / max.get() - min.get();
        thumb.setX(Math.min(Math.max(min.get(), x), max.get() - visibleAmount.get()));
    }


    /**
     * Clamp the value.
     * @param value the value
     * @return the clamped value
     */
    private double clamp(double value) {
        return Math.min(Math.max(min.get(), value), max.get());
    }


    /**
     * Set the handler.
     * @param handler the handler
     */
    public void setHandler(EventListener<ScrollBarChange> handler) {
        this.handler = handler;
    }


    /**
     * Set the layout width.
     * @param width the layout width
     */
    public void setLayoutWidth(double width) {
        setWidth(width);
    }


    public DoubleProperty maxProperty() { return max; }
    public DoubleProperty visibleAmountProperty() { return visibleAmount; }
    public DoubleProperty valueProperty() { return value; }

}
