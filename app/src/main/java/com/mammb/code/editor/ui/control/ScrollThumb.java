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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleRole;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import java.util.function.Function;

/**
 * ScrollThumb.
 * @author Naotsugu Kobayashi
 */
class ScrollThumb extends Rectangle {

    /** The length of thumb. */
    private final DoubleProperty length = new SimpleDoubleProperty(10);

    /** active?. */
    private final BooleanProperty active = new SimpleBooleanProperty(false);

    /** drag?. */
    private final BooleanProperty drag = new SimpleBooleanProperty(false);

    /** The point of drag start. */
    private final DoubleProperty dragPoint = new SimpleDoubleProperty(0);

    /** The Function of as point. */
    private final Function<Point2D, Double> asPoint;


    /**
     * Constructor.
     * @param width the width of thumb
     * @param height the height of thumb
     * @param asPoint the as point function
     */
    private ScrollThumb(double width, double height, Function<Point2D, Double> asPoint) {
        setManaged(false);
        setWidth(width);
        setHeight(height);
        setY(0);
        setArcHeight(8);
        setArcWidth(8);
        setFill(Colors.thumb);
        setAccessibleRole(AccessibleRole.THUMB);
        this.asPoint = asPoint;

        initListener();
    }


    /**
     * Initialize listener.
     */
    private void initListener() {
        active.addListener((ob, ov, nv) -> setFill(nv ? Colors.thumbActive : Colors.thumb));
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(this::handleMouseReleased);
    }


    /**
     * Create a thumb for row scroll bar.
     * @param width the width of thumb
     * @return a new thumb
     */
    public static ScrollThumb rowOf(double width) {
        var thumb = new ScrollThumb(width, 0, Point2D::getY);
        thumb.heightProperty().bind(thumb.length);
        return thumb;
    }


    /**
     * Create a thumb for col scroll bar.
     * @param height the height of thumb
     * @return a new thumb
     */
    public static ScrollThumb colOf(double height) {
        var thumb = new ScrollThumb(0, height, Point2D::getX);
        thumb.widthProperty().bind(thumb.length);
        return thumb;
    }


    /**
     * The handler of mouse dragged.
     * @param e the mouse event
     */
    private void handleMouseDragged(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }
        if (!drag.get()) {
            active.set(true);
            dragPoint.set(asPoint.apply(localToParent(e.getX(), e.getY())));
            drag.set(true);
        }
        dragPoint.set(asPoint.apply(localToParent(e.getX(), e.getY())));
        e.consume();
    }


    /**
     * The handler of mouse pressed.
     * @param e the mouse event
     */
    private void handleMousePressed(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }
        active.set(true);
        dragPoint.set(asPoint.apply(localToParent(e.getX(), e.getY())));
        drag.set(true);
        e.consume();
    }


    /**
     * The handler of mouse released.
     * @param e the mouse event
     */
    private void handleMouseReleased(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }
        drag.set(false);
        dragPoint.set(0);
    }

    // <editor-fold desc="properties">

    /**
     * The property of thumb length.
     * @return the property of thumb length
     */
    public final DoubleProperty lengthProperty() { return length; }

    /**
     * The property of thumb active.
     * @return the property of thumb active
     */
    public final BooleanProperty activeProperty() { return active; }

    /**
     * The property of drag.
     * @return the property of drag
     */
    public final BooleanProperty dragProperty() { return drag; }

    /**
     * The property of drag start point.
     * @return the property of drag start point
     */
    public final DoubleProperty dragPointProperty() { return dragPoint; }

    // </editor-fold>

}
