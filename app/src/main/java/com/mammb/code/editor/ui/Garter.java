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
package com.mammb.code.editor.ui;

import com.mammb.code.editor.ui.util.Colors;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Garter.
 * @author Naotsugu Kobayashi
 */
public class Garter {

    /** The left garter. */
    private final LeftGarter left;

    /** The top garter. */
    private final TopGarter top;


    /**
     * Constructor.
     */
    public Garter() {
        left = new LeftGarter();
        top = new TopGarter(left);
    }


    /**
     * Add node in the left garter.
     * @param node the node
     */
    public void addLeft(Node node) {
        left.add(node);
    }


    /**
     * Add node in the top garter.
     * @param node the node
     */
    public void addTop(Node node) {
        top.add(node);
    }


    /**
     * Apply to the BorderPane.
     * @param borderPane the BorderPane
     */
    public void apply(BorderPane borderPane) {
        borderPane.setTop(top);
        borderPane.setLeft(left);
    }


    /**
     * Get the left garter.
     * @return the left garter
     */
    public Region left() { return left; }


    /**
     * Get the top garter.
     * @return the top garter
     */
    public Region top() { return top; }


    /**
     * LeftGarter.
     */
    static class LeftGarter extends StackPane {

        static final double MIN_WIDTH = 50;

        public LeftGarter() {
            setPrefWidth(MIN_WIDTH);
            setMaxSize(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE);
            setBackground(new Background(new BackgroundFill(Colors.panel, null, null)));
        }

        public void add(Node node) {
            getChildren().add(node);
        }

    }


    /**
     * TopGarter.
     */
    static class TopGarter extends StackPane {

        private static final double HEIGHT = 6.0;

        private final Rectangle left;

        public TopGarter(Pane leftPane) {
            setPrefHeight(HEIGHT);
            setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_PREF_SIZE);
            setAlignment(Pos.TOP_LEFT);
            //setBackground(new Background(new BackgroundFill(Colors.background, null, null)));

            left = new Rectangle(LeftGarter.MIN_WIDTH, HEIGHT, Colors.panel);
            left.heightProperty().bind(heightProperty());
            left.widthProperty().bind(leftPane.widthProperty());
            getChildren().add(left);
        }

        public void add(Node node) {
            node.layoutXProperty().bind(left.widthProperty());
            getChildren().add(node);
        }

    }

}
