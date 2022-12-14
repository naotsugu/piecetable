package com.mammb.code.editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;

public class Caret extends Path {

    private final Timeline timeline;


    public Caret() {

        setStrokeWidth(2);
        setStroke(Color.LIGHTYELLOW);
        setManaged(false);

        timeline = new Timeline();
        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> setVisible(!isVisible())));
        timeline.play();

    }


    void disable() {
        timeline.stop();
        setVisible(false);
        getElements().remove(0, getElements().size());
    }


    void setShape(PathElement... elements) {

        if (elements == null || elements.length == 0) {
            disable();
            return;
        }

        if (elements[1] instanceof LineTo lineTo) {
            lineTo.setY(lineTo.getY() - 2);
        }
        timeline.stop();
        setVisible(true);
        getElements().setAll(elements);
        timeline.play();
    }

}
