package com.mammb.code.editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;

public class Caret extends Path {

    private final Timeline timeline;

    public Caret() {

        setStrokeWidth(2);
        setStroke(Color.WHITESMOKE);
        setManaged(false);

        timeline = new Timeline();
        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> setVisible(!isVisible())));
        timeline.play();

    }

    private void handleShape(PathElement... elements) {
        timeline.stop();
        setVisible(true);
        getElements().setAll(elements);
        timeline.play();
    }

}
