package com.mammb.code.editor;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LeftBar extends Region {

    public LeftBar(Font font) {
        setBackground(new Background(new BackgroundFill(Color.web("#303841"), null, null)));
        setPrefWidth(6);
    }

}
