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
import javafx.geometry.Pos;
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

    /** The text flow pane. */
    private final TextFlow textFlow = new TextFlow();

    /** The ui caret. */
    private final UiCaret caret = new UiCaret(textFlow);

    /** The text model. */
    private TextView model;


    /**
     * Constructor.
     * @param model the text view model
     */
    public TextPane(Stage stage, TextView model) {

        this.stage = Objects.requireNonNull(stage);
        this.model = Objects.requireNonNull(model);

        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(textFlow, caret);
        initHandler();
        initListener();
    }


    private void initHandler() {
        setOnKeyPressed(KeyPressedHandler.of(this));
        setOnKeyTyped(KeyTypedHandler.of());
        setOnScroll(ScrollHandler.of(textFlow, caret, model.scrollBehavior()));

        setOnDragOver(DragDrop.dragOverHandler());
        setOnDragDropped(DragDrop.droppedHandler(this::open));
    }


    private void initListener() {
        heightProperty().addListener(this::handleHeightChanged);
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
        initHandler();
        sync();
        caret.reset();
    }


    private void handleHeightChanged(ObservableValue<? extends Number> observable,
            Number oldValue, Number newValue) {
        if (!newValue.equals(oldValue)) sync();
    }


    /**
     * Get the stage.
     * @return the stage
     */
    public Stage stage() { return stage; }


    /**
     * Get the caret.
     * @return the caret
     */
    public UiCaret caret() { return caret; }


    /**
     * Sync model to textFlow.
     */
    public void sync() {
        model.setupMaxRows(maxRows());
        textFlow.setAll(Texts.asText(model.text()));
    }


    /**
     * Get the max rows by bounds height.
     * @return the max rows
     */
    private int maxRows() {
        return (int) Math.ceil(getLayoutBounds().getHeight() / Texts.height);
    }

}
