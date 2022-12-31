package com.mammb.code.editor;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ScrollBar extends StackPane {

    private final double width = 8;
    private final Rectangle thumb;


    public ScrollBar() {

        setBackground(new Background(new BackgroundFill(Color.web("#626465", 0.1), null, null)));
        setWidth(width);

        this.thumb = new Rectangle(width, 100);
        this.thumb.setArcHeight(4);
        this.thumb.setArcWidth(4);
        this.thumb.setFill(Color.web("#626465", 0.5));
        getChildren().add(thumb);
    }



}
