/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.ui.util.Colors;
import com.mammb.code.editor3.ui.util.Texts;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * OverlayPane.
 * @author Naotsugu Kobayashi
 */
public class Overlay extends StackPane {

    /**
     * Constructor.
     */
    public Overlay() {
        setAlignment(Pos.CENTER);
        setBackground(new Background(
            new BackgroundFill(Color.web("#000000", 0.4),
                CornerRadii.EMPTY, Insets.EMPTY)));
        setVisible(false);
    }


    /**
     * Clear overlay.
     */
    public void clear() {
        getChildren().clear();
        setVisible(false);
    }


    /**
     * Are you sure you want to discard your changes?
     * @param contentText
     */
    private void confirm(String contentText) {

        Text text = Texts.asText(contentText);
        double width  = text.getLayoutBounds().getWidth() + 50;
        double height = 120;

        HBox headerBox = new HBox();

        HBox contentBox = new HBox(text);
        contentBox.setPadding(new Insets(20));

        HBox footerBox = new HBox();
        footerBox.setSpacing(20);
        footerBox.setPadding(new Insets(20));
        footerBox.setAlignment(Pos.CENTER_RIGHT);

        footerBox.getChildren().addAll(button(" OK "), button("Cancel"));

        VBox mainContainer = new VBox();
        mainContainer.setMaxSize(width, height);
        mainContainer.setPrefSize(width, height);
        mainContainer.getChildren().addAll(headerBox, contentBox, footerBox);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);

        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setArcWidth(10);
        rectangle.setArcHeight(10);
        rectangle.setFill(Colors.background);
        rectangle.setStroke(Color.web("#4B4B4B"));
        rectangle.setOpacity(0.8);

        stackPane.getChildren().addAll(rectangle, mainContainer);

        getChildren().add(stackPane);
    }


    /**
     * Create button.
     * @param caption the caption
     * @return the button
     */
    private Node button(String caption) {
        StackPane button = new StackPane(Texts.asText(caption));
        button.setAlignment(Pos.CENTER);
        button.setPadding(new Insets(8));
        setBackgroundColor(button, "#424445");
        button.setOnMouseEntered(e -> setBackgroundColor(button, "#626465"));
        button.setOnMouseExited(e -> setBackgroundColor(button, "#424445"));
        return button;
    }


    /**
     * Set background color.
     * @param region the region
     * @param colorString the color string
     */
    private void setBackgroundColor(Region region, String colorString) {
        region.setBackground(new Background(
            new BackgroundFill(Color.web(colorString),
                new CornerRadii(3), Insets.EMPTY)));
    }

}
