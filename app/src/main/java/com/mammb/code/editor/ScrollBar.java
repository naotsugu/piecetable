package com.mammb.code.editor;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ScrollBar extends StackPane {

    private final Rectangle thumb;
    private final Node parent;

    public ScrollBar(Node parent) {
        setManaged(false);
        this.parent = parent;
        this.thumb = new Rectangle(8, 100);
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(Color.web("#626465", 0.1));
        getChildren().add(thumb);

    }

}
