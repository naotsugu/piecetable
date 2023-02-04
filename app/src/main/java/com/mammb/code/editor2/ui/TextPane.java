package com.mammb.code.editor2.ui;

import com.mammb.code.editor2.model.TextView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Objects;

public class TextPane extends StackPane {

    private TextView textView;
    private TextFlow textFlow = new TextFlow();

    public TextPane(TextView textView) {
        this.textView = Objects.requireNonNull(textView);
        getChildren().add(textFlow);
    }

    void fill() {
        textFlow.set(List.of(new Text(textView.string())));
    }

}
