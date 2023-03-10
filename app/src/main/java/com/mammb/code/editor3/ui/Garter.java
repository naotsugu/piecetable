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
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Garter.
 * @author Naotsugu Kobayashi
 */
public class Garter {

    private final Pane left;
    private final Pane top;


    /**
     * Constructor.
     * @param leftPanel the left panel
     */
    public Garter(Pane leftPanel) {
        left = new LeftGarter(leftPanel);
        top = new TopGarter(left);
    }


    /**
     * LeftGarter.
     */
    static class LeftGarter extends StackPane {

        static final double MIN_WIDTH = 50;

        public LeftGarter(Pane leftPanel) {
            setPrefWidth(MIN_WIDTH);
            setMaxSize(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE);
            setBackground(new Background(new BackgroundFill(Colors.panel, null, null)));
            getChildren().add(leftPanel);
        }

    }


    /**
     * TopGarter.
     */
    static class TopGarter extends StackPane {

        private static final double HEIGHT = 4.0;

        public TopGarter(Pane leftPane) {
            setPrefHeight(HEIGHT);
            setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE);
            setAlignment(Pos.TOP_LEFT);
            setBackground(new Background(new BackgroundFill(Colors.background, null, null)));

            Rectangle left = new Rectangle(LeftGarter.MIN_WIDTH, HEIGHT, Colors.panel);
            left.heightProperty().bind(heightProperty());
            left.widthProperty().bind(leftPane.widthProperty());
            getChildren().add(left);
        }

    }

}
