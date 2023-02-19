package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.ui.util.Colors;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class TopGarter extends StackPane {

    public TopGarter() {
        setPrefHeight(4);
        setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE);
        setBackground(new Background(new BackgroundFill(Colors.background, null, null)));
        //Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
        //    new BorderWidths(0, 0, 1, 0, false, false, false, false)));
        //setBorder(border);
    }

}
