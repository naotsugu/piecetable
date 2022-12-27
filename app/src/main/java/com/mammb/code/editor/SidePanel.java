package com.mammb.code.editor;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SidePanel extends StackPane {

    private final Text text;
    private final ScreenBuffer screenBuffer;

    public SidePanel(ScreenBuffer screenBuffer) {

        this.screenBuffer = screenBuffer;
        double width = Utils.getTextWidth(Fonts.main, 6);

        setBackground(new Background(new BackgroundFill(Colors.bgColor, null, null)));
        setStyle("-fx-border-width: 0 1 0 0; -fx-border-color: #4d4d4d;");
        setPrefWidth(width);

        text = new Text();
        text.setFont(Fonts.main);
        text.setFill(Colors.fgColor);

        TextFlow flow = new TextFlow(text);
        flow.setTextAlignment(TextAlignment.RIGHT);
        getChildren().add(flow);

        screenBuffer.originRowIndexProperty().addListener(this::handleLineMoved);
        screenBuffer.screenRowSizeProperty().addListener(this::handleLineMoved);
        screenBuffer.addListChangeListener(this::handleScreenTextChanged);
        fill(0, 1);
    }


    void handleLineMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        fill(screenBuffer.getOriginRowIndex(), screenBuffer.rows.size());
    }

    void handleScreenTextChanged(ListChangeListener.Change<? extends String> change) {
        while (change.next()) {
            if (change.wasRemoved() || change.wasAdded()) {
                fill(screenBuffer.getOriginRowIndex(), screenBuffer.rows.size());
            }
        }
    }


    private void fill(int start, int length) {
        text.setText(IntStream.range(start + 1, start + 1 + length)
            .mapToObj(String::valueOf)
            .map(str -> str + ' ')
            .collect(Collectors.joining("\n")));
    }


}
