package com.mammb.code.editor;

import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SidePanel extends StackPane {

    private static final Color bgColor = Color.web("#303841");
    private static final Color fgColor = Color.web("#939ea9");
    private static final Font font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);

    private final Text text;
    private final ScreenBuffer screenBuffer;

    public SidePanel(ScreenBuffer screenBuffer) {

        this.screenBuffer = screenBuffer;
        double width = Utils.getTextWidth(font, 6);

        setBackground(new Background(new BackgroundFill(bgColor, null, null)));
        setStyle("-fx-border-width: 0 1 0 0; -fx-border-color: #4d4d4d;");
        setPrefWidth(width);

        text = new Text();
        text.setFont(font);
        text.setFill(fgColor);

        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.RIGHT);
        getChildren().add(flow);

        screenBuffer.originRowIndexProperty().addListener(this::handleLineMoved);
        screenBuffer.screenRowSizeProperty().addListener(this::handleLineMoved);
        fill(0, 1);
    }


    void handleLineMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        fill(screenBuffer.getOriginRowIndex(), screenBuffer.rows.size());
    }


    private void fill(int start, int length) {
        text.setText(IntStream.range(start + 1, start + 1 + length)
            .mapToObj(String::valueOf)
            .map(str -> str + ' ')
            .collect(Collectors.joining("\n")));
    }


}
