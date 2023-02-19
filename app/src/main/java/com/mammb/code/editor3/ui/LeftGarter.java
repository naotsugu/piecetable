package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.ui.util.Colors;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class LeftGarter extends StackPane {
    public LeftGarter() {
        setPrefWidth(100);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE);
        setBackground(new Background(new BackgroundFill(Colors.background, null, null)));

    }
}
