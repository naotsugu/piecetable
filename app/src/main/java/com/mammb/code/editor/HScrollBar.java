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

    private final ScreenBuffer screenBuffer;
    private final double HEIGHT = 8;
    private final Rectangle thumb;
    private Point2D dragStart;

    private final DoubleProperty min = new SimpleDoubleProperty(0);
    private final DoubleProperty max = new SimpleDoubleProperty(0);
    private final DoubleProperty value = new SimpleDoubleProperty(0);
    private final DoubleProperty thumbLength = new SimpleDoubleProperty(0);


    public HScrollBar(ScreenBuffer screenBuffer) {

        this.screenBuffer = screenBuffer;

        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(Colors.hTtrackColor, null, null)));
        setHeight(HEIGHT + 4);
        setPrefHeight(HEIGHT + 4);

        this.thumb = new Rectangle(0, HEIGHT);
        this.thumb.setY(2);
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(Colors.thumbColor);
        this.thumb.setManaged(false);
        getChildren().add(thumb);

        setOnMouseEntered(e -> thumb.setFill(Colors.thumbActiveColor));
        setOnMouseExited(e  -> { if(dragStart == null) thumb.setFill(Colors.thumbColor); });

        thumb.setOnMousePressed(this::handleThumbMousePressed);
        thumb.setOnMouseReleased(this::handleThumbMouseReleased);
        thumb.setOnMouseDragged(this::handleThumbDragged);

        value.addListener(this::handleValueChanged);
        thumbLength.addListener((observable, oldValue, newValue) -> applyThumbWidth());
        max.addListener((observable, oldValue, newValue) -> applyThumbWidth());

    }

    private void applyThumbWidth() {
        double len = max.get() - min.get();
        thumb.setWidth(getWidth() * thumbLength.get() / Math.max(len, thumbLength.get()));
    }

    private void handleValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double len = getMax() - getMin();
        double range = getWidth() - thumb.getWidth();
        double x = range * clamp(getMin(), newValue.doubleValue(), getMax()) / len;
        thumb.setX(clamp(0, x, range));
    }

    private void handleThumbDragged(MouseEvent e) {
        if (e.isSynthesized()) {
            e.consume();
            return;
        }

        Point2D cur = thumb.localToParent(e.getX(), e.getY());
        if (dragStart == null) {
            markThumbStart(e);
        }
        double len = getMax() - getMin();
        double x = getMin() + len * (cur.getX() - dragStart.getX()) / (getWidth() - thumb.getWidth());
        value.setValue(clamp(getMin(), x, getMax()));
        e.consume();
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


    private void markThumbStart(MouseEvent e) {
        dragStart = thumb.localToParent(e.getX(), e.getY());
        thumb.setFill(Colors.thumbActiveColor);
    }


    private void markThumbEnd(MouseEvent e) {
        dragStart = null;
        thumb.setFill(Colors.thumbColor);
    }

    private static double clamp(double min, double value, double max) {
        return Math.min(Math.max(value, min), max);
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

    public final void setThumbLength(double value) { thumbLengthProperty().set(value); }
    public final double getThumbLength() { return thumbLength == null ? 10 : thumbLength.get(); }
    public final DoubleProperty thumbLengthProperty() { return thumbLength; }

    // </editor-fold>


}
