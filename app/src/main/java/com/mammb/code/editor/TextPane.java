package com.mammb.code.editor;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class TextPane extends Region {

    private final Color backgroundColor = Color.web("#303841");
    private final Color fontColor = Color.web("#d3dee9");

    private final Text text;
    private final Font font;
    private final Caret caret;

    private final LeftBar left;
    private final Pane main;

    private final Rectangle surface;


    public TextPane() {
        text = new Text();
        font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);
        left = new LeftBar(font);
        main = new Pane();
        surface = new Rectangle(
            Screen.getPrimary().getBounds().getWidth(),
            Screen.getPrimary().getBounds().getHeight());
        caret = new Caret(text);
        initComponent();
    }


    private void initComponent() {
        text.setFont(font);
        text.setTabSize(4);
        text.setLayoutY(text.getBaselineOffset() * 1.3);

        main.getChildren().addAll(surface, caret);
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(left);
        borderPane.setCenter(main);

        this.setBackground(new Background(new BackgroundFill(fontColor, null, null)));
        this.getChildren().add(borderPane);
    }


    public void setText(String str) {
        text.setText(str);
        Rectangle s = new Rectangle(
            Screen.getPrimary().getVisualBounds().getWidth(),
            Screen.getPrimary().getVisualBounds().getHeight());
        Shape shape = Shape.subtract(s, text);
        shape.setFill(backgroundColor);
        main.getChildren().remove(0);
        main.getChildren().add(0, shape);
    }


    public void setCaretPosition(int pos) {
        text.setCaretPosition(pos);
    }

}
