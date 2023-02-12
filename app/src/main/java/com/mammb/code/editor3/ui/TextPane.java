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

import com.mammb.code.editor3.model.TextView;
import com.mammb.code.editor3.ui.handler.DragDrop;
import com.mammb.code.editor3.ui.handler.KeyPressedHandler;
import com.mammb.code.editor3.ui.handler.KeyTypedHandler;
import com.mammb.code.editor3.ui.handler.ScrollHandler;
import com.mammb.code.editor3.ui.util.Texts;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.Objects;

/**
 * TextPane.
 * @author Naotsugu Kobayashi
 */
public class TextPane extends StackPane {

    /** The stage. */
    private final Stage stage;

    /** The text model. */
    private TextView model;

    /** The text flow pane. */
    private final TextFlow textFlow = new TextFlow();


    /**
     * Constructor.
     * @param model the text view model
     */
    public TextPane(Stage stage, TextView model) {

        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);

        this.stage = Objects.requireNonNull(stage);
        this.model = Objects.requireNonNull(model);
        getChildren().add(textFlow);

        initHandler();
        initListener();
    }


    private void initHandler() {
        setOnKeyPressed(KeyPressedHandler.of(this));
        setOnKeyTyped(KeyTypedHandler.of());
        setOnScroll(ScrollHandler.of());
        setOnDragOver(DragDrop.dragOverHandler());
        setOnDragDropped(DragDrop.droppedHandler(this::open));
    }


    private void initListener() {
        boundsInParentProperty().addListener(this::handleBoundsChanged);
    }


    /**
     * Open the file content path.
     * @param path the file content path
     */
    public void open(Path path) {
        if (model.isDirty()) {
            // TODO
        }
        model = new TextView(path);
        model.setupMaxRows(maxRows());
        textFlow.set(model.text());
    }


    private void handleBoundsChanged(
            ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        if (oldValue.getHeight() != newValue.getHeight()) {
            model.setupMaxRows(maxRows());
            textFlow.set(model.text());
        }
    }


    /**
     * Get the stage.
     * @return the stage
     */
    public Stage stage() { return stage; }


    /**
     * Get the max rows by bounds height.
     * @return the max rows
     */
    private int maxRows() {
        return (int) Math.ceil(getBoundsInParent().getHeight() / Texts.height);
    }

}
