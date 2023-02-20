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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * TopGarter.
 * @author Naotsugu Kobayashi
 */
public class TopGarter extends StackPane {

    private static final double HEIGHT = 4.0;

    private final Rectangle left;


    public TopGarter() {

        setPrefHeight(HEIGHT);
        setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE);
        setAlignment(Pos.TOP_LEFT);
        setBackground(new Background(new BackgroundFill(Colors.background, null, null)));

        left = new Rectangle(LeftGarter.MIN_WIDTH, HEIGHT, Colors.panel);
        left.heightProperty().bind(heightProperty());
        getChildren().add(left);

    }


    public DoubleProperty leftWidthProperty() { return left.widthProperty(); }

}
