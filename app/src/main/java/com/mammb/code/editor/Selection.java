package com.mammb.code.editor;

import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import java.util.function.BiFunction;

public class Selection extends Path {

    private int open = -1;
    private int close = -1;
    private boolean on = false;
    private boolean dragging = false;

    public Selection() {
        setLayoutX(4);
        setLayoutY(2);
        setFill(Color.DODGERBLUE);
        setStrokeWidth(0);
        setOpacity(0.4);
        setBlendMode(BlendMode.LIGHTEN);
        setManaged(false);
    }

    public void start(int index) {
        open = close = index;
        on = true;
        dragging = false;
    }

    public void startDrag(int index) {
        start(index);
        dragging = true;
    }

    public void releaseDragging() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }


    void clear() {
        setVisible(false);
        open = close = -1;
        on = dragging = false;
        getElements().clear();
    }


    public void handleOriginMoved(int oldValue, int newValue) {
        if (on) {
            open += oldValue - newValue;
        }
    }


    public void handleCaretMoved(int newValue, BiFunction<Integer, Integer, PathElement[]> paths) {
        if (on) {
            close = newValue;
            if (open < close) {
                setShape(paths.apply(open, close));
            } else if (open > close)  {
                setShape(paths.apply(close, open));
            }
        }
    }


    void setShape(PathElement... elements) {
        getElements().setAll(elements);
        setVisible(true);
    }

    public boolean on() {
        return on;
    }

    public int getOpen() {
        return open;
    }

    public int getClose() {
        return close;
    }
}
