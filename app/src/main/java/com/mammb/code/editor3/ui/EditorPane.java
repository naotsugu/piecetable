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

import com.mammb.code.editor3.model.TextModel;
import com.mammb.code.editor3.ui.util.Colors;
import com.mammb.code.editor3.ui.util.Keys;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.lang.System.Logger;
import java.lang.System.Logger.*;

/**
 * EditorPane.
 * @author Naotsugu Kobayashi
 */
public class EditorPane extends StackPane {

    /** The logger. */
    private static final Logger log = System.getLogger(EditorPane.class.getName());

    /** The stage. */
    private final Stage stage;

    /** The text pane. */
    private final TextPane textPane;

    /** The overlay pane. */
    private final Overlay overlay;


    /**
     * Constructor.
     */
    public EditorPane(Stage stage) {
        this.stage = stage;
        this.overlay = new Overlay();
        this.textPane = new TextPane(stage, overlay, new TextModel());

        BorderPane layout = new BorderPane();
        layout.setCenter(textPane);
        initGarter(layout);
        setOnKeyPressed(e -> { if (Keys.SC_N.match(e)) newPane(); });
        getChildren().addAll(layout, overlay);
    }


    /**
     * Initialize garter.
     */
    private void initGarter(BorderPane layout) {
        Garter garter = new Garter();
        garter.addLeft(textPane.rowsPanel());
        garter.apply(layout);
        textPane.prefHeightProperty().bind(heightProperty()
            .subtract(garter.top().heightProperty()));
        textPane.prefWidthProperty().bind(widthProperty()
            .subtract(garter.left().widthProperty()));
    }


    /**
     * Show stage.
     */
    public void show() {
        Scene scene = new Scene(this, 800, 480);
        scene.setFill(Colors.background);
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Create the new pane.
     */
    public void newPane() {
        Stage newStage = new Stage();
        log.log(Level.INFO, "invoked newPane[{}]", newStage);
        newStage.setX(stage.getX() + 15);
        newStage.setY(stage.getY() + 15);
        new EditorPane(newStage).show();
    }

}
