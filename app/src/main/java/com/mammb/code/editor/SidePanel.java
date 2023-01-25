package com.mammb.code.editor;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import java.util.List;

public class SidePanel extends StackPane {

    public SidePanel() {
        double width = Utils.getTextWidth(Fonts.main, 6);
        setBackground(new Background(new BackgroundFill(Colors.bgColor, null, null)));
        setStyle("-fx-border-width: 0 1 0 0; -fx-border-color: #4d4d4d;");
        setPrefWidth(width);
        setWidth(width);
        setPadding(new Insets(4));
    }


    void drawNumber(int start, List<Point2D> linePoints) {
        getChildren().clear();
        for (Point2D point : linePoints) {
            Text text = new Text(String.format("%4s", ++start));
            text.setFont(Fonts.main);
            text.setFill(Colors.darkColor);
            text.setX(point.getX() + 10);
            text.setY(point.getY());
            text.setManaged(false);
            getChildren().add(text);
        }
    }

}
