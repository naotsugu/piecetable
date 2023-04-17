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
import com.mammb.code.editor3.ui.behavior.CaretBehavior;
import com.mammb.code.editor3.ui.behavior.ConfBehavior;
import com.mammb.code.editor3.ui.behavior.FileBehavior;
import com.mammb.code.editor3.ui.behavior.EditBehavior;
import com.mammb.code.editor3.ui.behavior.ImeBehavior;
import com.mammb.code.editor3.ui.behavior.ScrollBehavior;
import com.mammb.code.editor3.ui.handler.DragDrop;
import com.mammb.code.editor3.ui.handler.ImeTextHandler;
import com.mammb.code.editor3.ui.handler.KeyPressedHandler;
import com.mammb.code.editor3.ui.handler.KeyTypedHandler;
import com.mammb.code.editor3.ui.handler.MouseClickedHandler;
import com.mammb.code.editor3.ui.handler.MouseDraggedHandler;
import com.mammb.code.editor3.ui.handler.ScrollHandler;
import com.mammb.code.editor3.ui.util.Texts;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.Region;
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
    private final TextFlow textFlow;

    /** The pointing. */
    private final Pointing pointing;

    /** The screen bound. */
    private final ScreenBound screenBound;

    /** The rows panel. */
    private final RowsPanel rowsPanel;

    /** The text model. */
    private TextModel model;


    /**
     * Constructor.
     * @param model the text view model
     */
    public TextPane(Stage stage, TextModel model) {

        this.stage = Objects.requireNonNull(stage);
        this.model = Objects.requireNonNull(model);

        this.textFlow = new TextFlow();
        this.pointing = new Pointing(textFlow);
        this.screenBound = new ScreenBound(this, textFlow);
        this.rowsPanel = new RowsPanel(textFlow, screenBound);

        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setAlignment(Pos.TOP_LEFT);

        Scrolling scrolling = new Scrolling(screenBound);
        getChildren().addAll(textFlow, pointing, scrolling);
        initHandler();
        initListener();

    }


    /**
     * Initialize handler.
     */
    private void initHandler() {

        ScrollBehavior scrollBehavior = new ScrollBehavior(textFlow, pointing, model, screenBound);
        CaretBehavior caretBehavior = new CaretBehavior(pointing, scrollBehavior, heightProperty(), widthProperty());
        EditBehavior editBehavior = new EditBehavior(model, pointing, textFlow, rowsPanel, caretBehavior, scrollBehavior);
        FileBehavior fileBehavior = new FileBehavior(this);
        ConfBehavior confBehavior = new ConfBehavior(textFlow, pointing, rowsPanel);
        ImeBehavior imeBehavior = new ImeBehavior(textFlow, pointing, editBehavior);

        setOnKeyTyped(KeyTypedHandler.of(editBehavior));
        setOnKeyPressed(KeyPressedHandler.of(
            caretBehavior, scrollBehavior, editBehavior, imeBehavior, fileBehavior, confBehavior));

        setOnScroll(ScrollHandler.of(scrollBehavior));

        setOnDragOver(DragDrop.dragOverHandler());
        setOnDragDropped(DragDrop.droppedHandler(this::open));

        setOnMouseClicked(MouseClickedHandler.of(caretBehavior));
        setOnMouseDragged(MouseDraggedHandler.of(caretBehavior));

        setInputMethodRequests(imeBehavior.inputMethodRequests());
        setOnInputMethodTextChanged(ImeTextHandler.of(imeBehavior));
    }


    /**
     * Initialize listener.
     */
    private void initListener() {
        layoutBoundsProperty().addListener(this::layoutBoundsChanged);
        stage.focusedProperty().addListener((ob, ov, focused) -> {
            if (focused) pointing.showCaret(); else pointing.hideCaret();
        });
    }


    /**
     * Open the file content path.
     * @param path the file content path
     */
    public void open(Path path) {
        model.open(path);
        sync();
        pointing.clear();
    }


    /**
     * Save.
     */
    public void save() {
        model.save();
    }


    /**
     * Save as.
     */
    public void saveAs(Path path) {
        model.saveAs(path);
        setupStageTitle();
    }


    /**
     * Called when the value of the layout changes.
     * @param observable the ObservableValue which value changed
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void layoutBoundsChanged(
            ObservableValue<? extends Bounds> observable,
            Bounds oldValue, Bounds newValue) {
        if (oldValue.getHeight() != newValue.getHeight() ||
            oldValue.getWidth()  != newValue.getWidth()) sync();
    }


    /**
     * Get the stage.
     * @return the stage
     */
    public Stage stage() { return stage; }


    /**
     * Sync model to textFlow.
     */
    public void sync() {
        model.setupMaxRows(maxRows());
        textFlow.setAll(Texts.asText(model.text()));
        textFlow.clearTranslation();

        int caretOffset = pointing.caretOffset();
        pointing.clear();
        pointing.addOffset(caretOffset);

        screenBound.setTotalRowSize(model.totalRowSize() + textFlow.wrappedLines());
        screenBound.setRowOffset(model.originRowIndex(), textFlow.translatedLineOffset());
        rowsPanel.redraw();

        setupStageTitle();
    }


    /**
     * Setup stage title.
     */
    private void setupStageTitle() {
        Path path = model.contentPath();
        String title = (path == null) ? "untitled" : path.toString();
        title += model.isDirty() ? " *" : "";
        stage.setTitle(title);
    }


    /**
     * Gets whether this text is dirty.
     * @return {@code true} if text is dirty.
     */
    public boolean isDirty() { return model.isDirty(); }


    /**
     * Get the content path.
     * @return the content path. {@code null} if content path is empty
     */
    public Path contentPath() {
        return model.contentPath();
    }


    /**
     * Get the max rows by bounds height.
     * @return the max rows
     */
    private int maxRows() {
        return (int) Math.ceil(getHeight() / Texts.height);
    }


    /**
     * Get the rows panel.
     * @return the rows panel
     */
    RowsPanel rowsPanel() { return rowsPanel; }

}
