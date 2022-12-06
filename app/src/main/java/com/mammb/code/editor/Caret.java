package com.mammb.code.editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class Caret extends Path {

    private final Timeline timeline;

    public Caret(Text text) {
        setStrokeWidth(2);
        setStroke(Color.LIGHTYELLOW);
        setManaged(false);
        text.caretShapeProperty().addListener((o, oldVal, newVal) -> handleShape(newVal));
        setLayoutY(text.getBaselineOffset());
        timeline = new Timeline();
        timeline.setCycleCount(-1);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> setVisible(!isVisible())));
        timeline.play();
    }

    private void handleShape(PathElement... elements) {
        if (elements[0] instanceof MoveTo e) {
            e.setY(e.getY() + 1);
        }
        if (elements[1] instanceof LineTo e) {
            e.setY(e.getY() - 1);
        }
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
