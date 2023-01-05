package com.mammb.code.editor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleRole;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ScrollBar extends StackPane {

    private static final Color color = Color.web("#626465", 0.2);
    private static final Color thumbColor = Color.web("#626465", 0.5);
    private static final Color thumbActiveColor = Color.web("#828485", 0.9);

    private final ScreenBuffer screenBuffer;
    private final double WIDTH = 8;
    private final Rectangle thumb;
    private Point2D dragStart;
    private int dragStartRowIndex;

    private final DoubleProperty min = new SimpleDoubleProperty(0);
    private final DoubleProperty max = new SimpleDoubleProperty(0);
    private final DoubleProperty value = new SimpleDoubleProperty(0);
    private final DoubleProperty thumbLength = new SimpleDoubleProperty(0);


    public ScrollBar(ScreenBuffer screenBuffer) {

        this.screenBuffer = screenBuffer;

        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(color, null, null)));
        setWidth(WIDTH);

        this.thumb = new Rectangle(WIDTH, 0);
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(thumbColor);
        this.thumb.setY(0);
        this.thumb.setManaged(false);
        getChildren().add(thumb);

        setOnMouseEntered(e -> thumb.setFill(thumbActiveColor));
        setOnMouseExited(e  -> thumb.setFill(thumbColor));

        thumb.setOnMousePressed(this::handleThumbMousePressed);
        thumb.setOnMouseDragged(this::handleThumbDragged);
        setOnMouseClicked(this::handleTruckClicked);

        value.addListener(this::handleValueChanged);

        thumbLength.addListener((observable, oldValue, newValue) -> applyThumbHeight());
        max.addListener((observable, oldValue, newValue) -> applyThumbHeight());
    }


    private void handleValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double len = getMax() - getMin();
        double range = getHeight() - thumb.getHeight();
        double y = range * clamp(getMin(), newValue.doubleValue(), getMax()) / len;
        thumb.setY(clamp(0, y, range));
    }


    private void handleTruckClicked(MouseEvent e) {

        if (!e.getSource().equals(this) || e.getButton() != MouseButton.PRIMARY) {
            return;
        }

        if (e.getY() < thumb.getY() ) {
            screenBuffer.pageScrollUp();
        } else if (e.getY() > thumb.getY() + thumb.getHeight()) {
            screenBuffer.pageScrollDown();
        }
        e.consume();
    }


    private void handleThumbDragged(MouseEvent e) {

        if (e.isSynthesized()) {
            // touch-screen events handled by Scroll handler
            e.consume();
            return;
        }

        Point2D cur = thumb.localToParent(e.getX(), e.getY());
        if (dragStart == null) {
            markThumbStart(e);
        }
        double len = getMax() - getMin();
        double y = getMin() + len * (cur.getY() - dragStart.getY()) / (getHeight() - thumb.getHeight());

        screenBuffer.scrollTo(dragStartRowIndex + (int) Math.floor(y));
        e.consume();
    }


    private void handleThumbMousePressed(MouseEvent e) {
        if (e.isSynthesized()) {
            // touch-screen events handled by Scroll handler
            e.consume();
            return;
        }
        markThumbStart(e);
        e.consume();
    }


    private void markThumbStart(MouseEvent e) {
        dragStart = thumb.localToParent(e.getX(), e.getY());
        dragStartRowIndex = screenBuffer.getOriginRowIndex();
    }


    private void applyThumbHeight() {
        thumb.setHeight(getHeight() * getThumbLength() / Math.max(getMax() - getMin(), getThumbLength()));
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
