/*
 * Copyright 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.ui.control;

import com.mammb.code.editor.ui.util.Texts;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.function.Consumer;

/**
 * FlatDialog.
 * @author Naotsugu Kobayashi
 */
public class FlatDialog extends GridPane {

    /**
     * Constructor.
     * @param headerText the header text
     * @param contentText the content text
     * @param buttons the buttons
     */
    private FlatDialog(String headerText, String contentText, Node... buttons) {

        Text hText = headerText(headerText);
        Text cText = contentText(contentText);
        HBox buttonBar = new HBox(buttons);
        buttonBar.setSpacing(20);

        double width = Math.max(hText.getLayoutBounds().getWidth(),
                                cText.getLayoutBounds().getWidth());
        double height = hText.getLayoutBounds().getHeight() +
                        cText.getLayoutBounds().getHeight() +
                        buttonBar.getHeight() + 100;

        setBackground(new Background(new BackgroundFill(
            Color.web("#2B2B2B", 0.8),
            new CornerRadii(3),
            Insets.EMPTY)));

        setBorder(new Border(new BorderStroke(
            Color.web("#4B4B4B"),
            BorderStrokeStyle.SOLID,
            new CornerRadii(10),
            BorderWidths.DEFAULT,
            Insets.EMPTY)));

        setPadding(new Insets(15));
        setHgap(10);
        setVgap(10);

        setMaxSize(width, height);
        setPrefSize(width, height);


        //      0     1     2
        // 0 |     |     |     |
        // 1 |      text       |
        // 2 |     |     | btn |
        add(hText,0, 0, 3, 1);
        add(cText,0, 1, 3, 1);
        add(buttonBar,2, 2);

        ColumnConstraints cca = new ColumnConstraints();
        cca.setHgrow(Priority.ALWAYS);
        ColumnConstraints ccs = new ColumnConstraints();
        ccs.setHgrow(Priority.SOMETIMES);
        getColumnConstraints().addAll(ccs, cca, ccs);

        RowConstraints rca  = new RowConstraints();
        rca.setVgrow(Priority.ALWAYS);
        RowConstraints rcs  = new RowConstraints();
        rcs.setVgrow(Priority.SOMETIMES);
        getRowConstraints().addAll(rcs, rca, rcs);

    }


    /**
     * Create a new confirm FlatDialog.
     * @param contentText the content text
     * @param ok ok action
     * @param close close action
     * @return the FlatDialog
     */
    public static FlatDialog confirmOf(
        String contentText, Runnable ok, Runnable close) {
        return new FlatDialog("Confirmation", contentText,
            button(" OK ", e -> { e.consume(); ok.run(); close.run();}),
            button("Cancel", e -> { e.consume(); close.run(); }));
    }


    /**
     * Create the header text.
     * @param headerText the text.
     * @return the header text
     */
    private static Text headerText(String headerText) {
        Text text = new Text(headerText);
        text.setFont(new Font(18));
        text.setFill(Color.WHITE);
        return text;
    }


    /**
     * Create the content text.
     * @param contentText the text.
     * @return the content text
     */
    private static Text contentText(String contentText) {
        Text text = new Text(contentText);
        text.setFont(new Font(14));
        text.setFill(Color.WHITESMOKE);
        text.setWrappingWidth(300);
        return text;
    }


    /**
     * Create button.
     * @param caption the caption
     * @return the button
     */
    private static Node button(
        String caption,
        EventHandler<MouseEvent> clickHandler) {

        StackPane button = new StackPane(Texts.asText(caption));

        Consumer<String> colorFn = colorString -> button.setBackground(
            new Background(new BackgroundFill(
                Color.web(colorString),
                new CornerRadii(3),
                Insets.EMPTY)));

        button.setAlignment(Pos.CENTER);
        button.setPadding(new Insets(6));
        colorFn.accept("#424445");
        button.setOnMouseEntered(e -> colorFn.accept("#626465"));
        button.setOnMouseExited(e -> colorFn.accept("#424445"));
        button.setOnMouseClicked(clickHandler);
        return button;
    }

}
