package com.mammb.code.editor;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SidePanel extends StackPane {

    private static final int padding = 4;

    public SidePanel() {
        double width = Utils.getTextWidth(Fonts.main, 6);
        setBackground(new Background(new BackgroundFill(Colors.bgColor, null, null)));
        setStyle("-fx-border-width: 0 1 0 0; -fx-border-color: #4d4d4d;");
        setPrefWidth(width);
        setWidth(width);
        getChildren().add(createText(1, 0));
    }


    void drawNumber(int start, List<Point2D> linePoints) {
        List<Text> texts = new ArrayList<>();
        for (Point2D point : linePoints) {
            texts.add(createText(++start, point.getY()));
        }
        getChildren().setAll(texts);
    }

    private Text createText(int n, double y) {
        Text text = new Text(String.format("%4s", n));
        text.setFont(Fonts.main);
        text.setFill(Colors.darkColor);
        text.setTextOrigin(VPos.TOP);
        text.setX(10);
        text.setY(y + padding);
        text.setManaged(false);
        return text;
    }

}
