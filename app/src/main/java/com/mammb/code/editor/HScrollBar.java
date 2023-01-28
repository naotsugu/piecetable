package com.mammb.code.editor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleRole;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class HScrollBar extends Region {

    private final double HEIGHT = 8;
    private final Rectangle thumb;

    private Point2D dragStart;
    private double preDragThumbPos;

    private final DoubleProperty min = new SimpleDoubleProperty(0);
    private final DoubleProperty max = new SimpleDoubleProperty(0);
    private final DoubleProperty value = new SimpleDoubleProperty(0);


    public HScrollBar() {

        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(Colors.hTtrackColor, null, null)));
        setHeight(HEIGHT + 4);
        setPrefHeight(HEIGHT + 4);

        this.thumb = new Rectangle(0, HEIGHT);
        this.thumb.setY(2);
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(Colors.thumbColor);
        this.thumb.setAccessibleRole(AccessibleRole.THUMB);
        this.thumb.setManaged(false);
        getChildren().add(thumb);

        setOnMouseEntered(e -> thumb.setFill(Colors.thumbActiveColor));
        setOnMouseExited(e  -> { if(dragStart == null) thumb.setFill(Colors.thumbColor); });

        thumb.setOnMousePressed(this::handleThumbMousePressed);
        thumb.setOnMouseReleased(this::handleThumbMouseReleased);
        thumb.setOnMouseDragged(this::handleThumbDragged);

        value.addListener(this::handleValueChanged);
        max.addListener((observable, oldValue, newValue) -> applyThumbWidth());

        setOnMouseClicked(this::handleTruckClicked);
    }


    void applyThumbWidth() {
        thumb.setWidth(Math.max(10, getWidth() * getWidth() / Math.max(max.get() - min.get(), getWidth())));
    }


    private void handleValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double val = clampValue(newValue.doubleValue());
        double x = (getWidth() - thumb.getWidth()) * (val - min.get()) / valueLength();
        thumb.setX(x);
    }

    private void handleThumbDragged(MouseEvent me) {
        if (me.isSynthesized()) {
            me.consume();
            return;
        }

        Point2D cur = thumb.localToParent(me.getX(), me.getY());
        if (dragStart == null) {
            markThumbStart(me);
        }

        double dragPos = cur.getX() - dragStart.getX();
        double position = preDragThumbPos + dragPos / (getWidth() - thumb.getWidth());
        double newValue = (position * valueLength()) + min.get();
        if (!Double.isNaN(newValue)) {
            value.setValue(clampValue(newValue));
        }
        me.consume();
    }


    private void handleThumbMousePressed(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }
        markThumbStart(e);
        e.consume();
    }


    private void handleThumbMouseReleased(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }
        markThumbEnd(e);
    }


    private void markThumbStart(MouseEvent me) {
        dragStart = thumb.localToParent(me.getX(), me.getY());
        double val = clampValue(value.get());
        preDragThumbPos = (val - min.get()) / valueLength();
        thumb.setFill(Colors.thumbActiveColor);
    }


    private void markThumbEnd(MouseEvent e) {
        dragStart = null;
        preDragThumbPos = 0;
        thumb.setFill(Colors.thumbColor);
    }

    private void handleTruckClicked(MouseEvent e) {

        if (e.isSynthesized()) {
            e.consume();
            return;
        }

        if (e.getX() < thumb.getX() ) {
            value.setValue(clampValue(value.getValue() - getWidth()));
        } else if (e.getX() > thumb.getX() + thumb.getWidth()) {
            value.setValue(clampValue(value.getValue() + getWidth()));
        }
        e.consume();
    }


    private double clampValue(double value) {
        return Math.min(Math.max(value, min.get()), max.get() - thumb.getWidth());
    }

    private double valueLength() {
        return max.get() - thumb.getWidth() - min.get();
    }

    // <editor-fold desc="properties">

    public final void setMin(double value) { minProperty().set(value); }
    public final double getMin() { return min == null ? 0 : min.get(); }
    public final DoubleProperty minProperty() { return min; }

    public final void setMax(double value) { maxProperty().set(value); }
    public final double getMax() { return max == null ? 100 : max.get(); }
    public final DoubleProperty maxProperty() { return max; }

    public final void setValue(double value) { valueProperty().set(value); }
    public final double getValue() { return value == null ? 0 : value.get(); }
    public final DoubleProperty valueProperty() { return value; }

    // </editor-fold>


}
