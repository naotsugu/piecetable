package com.mammb.code.editor2.ui;

import javafx.geometry.Insets;
import javafx.scene.text.Text;

import java.util.List;

public class TextFlow extends javafx.scene.text.TextFlow {

    public TextFlow() {
        setPadding(new Insets(4));
        setTabSize(4);
    }

    void set(List<Text> texts) {
        getChildren().setAll(texts);
    }

}
