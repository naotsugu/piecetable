package com.mammb.code.editor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class HScrollBar extends Region {

    private final ScreenBuffer screenBuffer;
    private final double HEIGHT = 8;
    private final Rectangle thumb;

    private final DoubleProperty min = new SimpleDoubleProperty(0);
    private final DoubleProperty max = new SimpleDoubleProperty(0);
    private final DoubleProperty value = new SimpleDoubleProperty(0);
    private final DoubleProperty thumbLength = new SimpleDoubleProperty(0);


    public HScrollBar(ScreenBuffer screenBuffer) {

        this.screenBuffer = screenBuffer;

        setAccessibleRole(AccessibleRole.SCROLL_BAR);
        setBackground(new Background(new BackgroundFill(Colors.hTtrackColor, null, null)));
        setHeight(HEIGHT + 4);

        this.thumb = new Rectangle(0, HEIGHT);
        this.thumb.setY(2);
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(Colors.thumbColor);
        this.thumb.setManaged(false);
        getChildren().add(thumb);

        thumbLength.addListener((observable, oldValue, newValue) -> applyThumbWidth());
        max.addListener((observable, oldValue, newValue) -> applyThumbWidth());

    }

    private void applyThumbWidth() {
        double len = max.get() - min.get();
        thumb.setWidth(getWidth() * thumbLength.get() / Math.max(len, thumbLength.get()));
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
