package com.mammb.code.editor;

import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

import java.util.ArrayList;
import java.util.List;

public class Selection extends Path {

    private final List<Group> groups;

    public Selection() {
        this.groups = new ArrayList<>();
        setLayoutX(4);
        setFill(Color.AQUA);
        setStrokeWidth(0);
        setOpacity(0.3);
        setBlendMode(BlendMode.LIGHTEN);
        setManaged(false);
    }

    void clear() {
        setVisible(false);
        getElements().remove(0, getElements().size());
    }

    void setShape(PathElement... elements) {
        setVisible(true);
        getElements().setAll(elements);
    }
}
