package com.mammb.code.editor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ScrollBar extends StackPane {

    private final double WIDTH = 8;
    private final Rectangle thumb;

    private final DoubleProperty min = new SimpleDoubleProperty(0);
    private final DoubleProperty max = new SimpleDoubleProperty();
    private final DoubleProperty value = new SimpleDoubleProperty();
    private final DoubleProperty thumbLength = new SimpleDoubleProperty();
    private final DoubleProperty blockIncrement = new SimpleDoubleProperty(0);


    public ScrollBar() {

        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(Color.web("#626465", 0.1), null, null)));
        setWidth(WIDTH);

        this.thumb = new Rectangle(WIDTH, getBlockIncrement());
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(Color.web("#626465", 0.5));
        this.thumb.setY(0);
        this.thumb.setManaged(false);
        getChildren().add(thumb);

        value.addListener((observable, oldValue, newValue) -> {
            double len = getMax() - getMin();
            double y = getHeight() * clamp(getMin(), newValue.doubleValue(), getMax()) / len;
            thumb.setY(clamp(0, y, getHeight() - getThumbLength()));
        });

        this.thumb.heightProperty().bind(thumbLength);

    }


    public void adjustValue(double position) {
        // figure out the "value" associated with the specified position
        double posValue = ((getMax() - getMin()) * clamp(0, position, 1)) + getMin();
        double newValue;
        if (Double.compare(posValue, getValue()) != 0) {
            if (posValue > getValue()) {
                newValue = getValue() + getBlockIncrement();
            }
            else {
                newValue = getValue() - getBlockIncrement();
            }
            setValue(clamp(getMin(), newValue, getMax()));
        }
    }


    public static double clamp(double min, double value, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
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

    public final void setBlockIncrement(double value) { blockIncrementProperty().set(value); }
    public final double getBlockIncrement() { return blockIncrement == null ? 10 : blockIncrement.get(); }
    public final DoubleProperty blockIncrementProperty() { return blockIncrement; }

    // </editor-fold>

}
