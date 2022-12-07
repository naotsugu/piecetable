package com.mammb.code.editor;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.nio.file.Path;

public class TextPane extends Region {

    private static final Color bgColor = Color.web("#303841");
    private static final Color fgColor = Color.web("#d3dee9");
    private static final Font font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);

    private TextFlow textFlow;
    private Content content;

    public TextPane() {

        setBackground(new Background(new BackgroundFill(bgColor, null, null)));

        textFlow = new TextFlow();
        textFlow.setTabSize(4);
        textFlow.setPadding(new Insets(4));
        getChildren().add(textFlow);

        content = new PtContent();
        content.open(Path.of("../README.md"));
        for (int i = 0; i < content.length(); i++) {
            String string = this.content.untilEol(i);
            i += string.length();
            Text text = new Text(string);
            text.setFont(font);
            text.setFill(fgColor);
            textFlow.getChildren().add(text);
        }

    }
}
