package com.mammb.code.editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;

public class Caret extends Path {

    private final Timeline timeline = new Timeline();

    private final ReadOnlyDoubleWrapper shapeX = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper shapeY = new ReadOnlyDoubleWrapper();


    public Caret() {

        setStrokeWidth(2);
        setStroke(Color.ORANGE);
        setManaged(false);

        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> setVisible(!isVisible())));
        timeline.play();

    }


    void pause(boolean pause) {
        if (pause) {
            timeline.stop();
        } else {
            timeline.play();
        }
    }


    void disable() {
        timeline.stop();
        setVisible(false);
        getElements().remove(0, getElements().size());
    }


    void setShape(PathElement... elements) {

        if (elements == null || elements.length == 0) {
            disable();
            shapeX.set(-1);
            shapeY.set(-1);
            return;
        }

        if (elements[0] instanceof MoveTo moveTo) {
            shapeX.set(moveTo.getX());
            shapeY.set(moveTo.getY());
        } else {
            shapeX.set(-1);
            shapeY.set(-1);
        }

        if (elements[1] instanceof LineTo lineTo) {
            lineTo.setY(lineTo.getY() - 2);
        }

        timeline.stop();
        getElements().setAll(elements);
        setVisible(true);
        timeline.play();
    }


    public final double getShapeX() { return shapeX.get(); }
    public ReadOnlyDoubleProperty shapeXProperty() { return shapeX; }
    public final double getShapeY() { return shapeY.get(); }
    public ReadOnlyDoubleProperty shapeYProperty() { return shapeY; }

}
