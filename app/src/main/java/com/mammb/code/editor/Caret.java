package com.mammb.code.editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Caret extends Path {

    private final Timeline timeline;

    public Caret(Text text) {
        setStrokeWidth(2);
        setStroke(Color.WHITESMOKE);
        setManaged(false);
        text.caretShapeProperty().addListener((o, oldVal, newVal) -> handleShape(newVal));
        setLayoutY(text.getBaselineOffset());
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

    public double getMaxX() {
        return getLayoutBounds().getMaxX();
    }

    public double getMaxY() {
        return getLayoutBounds().getMaxY();
    }

}
