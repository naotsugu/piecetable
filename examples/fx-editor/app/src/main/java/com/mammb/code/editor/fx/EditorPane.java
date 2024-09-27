/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.fx;

import com.mammb.code.editor.core.Action;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.ScreenScroll;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * The EditorPane.
 * @author Naotsugu Kobayashi
 */
public class EditorPane extends StackPane {

    /** The canvas. */
    private final Canvas canvas;
    /** The draw. */
    private final Draw draw;
    /** The editor model. */
    private EditorModel model;
    /** The vertical scroll bar. */
    private final ScrollBar vScroll = new ScrollBar();
    /** The horizon scroll bar. */
    private final ScrollBar hScroll = new ScrollBar();

    /**
     * Constructor.
     */
    public EditorPane() {

        canvas = new Canvas(640, 480);
        canvas.setFocusTraversable(true);
        draw = new FxDraw(canvas.getGraphicsContext2D());
        model = EditorModel.of(draw.fontMetrics(), screenScroll());
        vScroll.setOrientation(Orientation.VERTICAL);
        hScroll.setOrientation(Orientation.HORIZONTAL);
        StackPane.setAlignment(vScroll, Pos.TOP_RIGHT);
        StackPane.setAlignment(hScroll, Pos.BOTTOM_LEFT);
        getChildren().addAll(canvas, vScroll, hScroll);

        layoutBoundsProperty().addListener(this::handleLayoutBoundsChanged);
        setOnScroll(this::handleScroll);
        setOnMouseClicked(this::handleMouseClicked);
        setOnMouseDragged(this::handleMouseDragged);
        setOnKeyPressed(this::handleKeyAction);
        setOnKeyTyped(this::handleKeyAction);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);

        vScroll.valueProperty().addListener(this::handleVerticalScroll);
        hScroll.valueProperty().addListener(this::handleHorizontalScroll);
        canvas.setInputMethodRequests(inputMethodRequests());
        canvas.setOnInputMethodTextChanged(this::handleInputMethodTextChanged);
    }


    private void handleLayoutBoundsChanged(
            ObservableValue<? extends Bounds> ob, Bounds o, Bounds n) {
        canvas.setWidth(n.getWidth());
        canvas.setHeight(n.getHeight());
        model.setSize(n.getWidth(), n.getHeight());
        draw();
    }


    private void handleScroll(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL && e.getDeltaY() != 0) {
            if (e.getDeltaY() < 0) {
                model.scrollNext((int) Math.min(5, Math.abs(e.getDeltaY())));
            } else {
                model.scrollPrev((int) Math.min(5, e.getDeltaY()));
            }
            draw();
        }
    }


    private void handleMouseClicked(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && e.getTarget() == canvas) {
            switch (e.getClickCount()) {
                case 1 -> model.click(e.getX(), e.getY(), false);
                case 2 -> model.clickDouble(e.getX(), e.getY());
                case 3 -> model.clickTriple(e.getX(), e.getY());
            }
            draw();
        }
    }


    private void handleMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            model.moveDragged(e.getX(), e.getY());
            model.draw(draw);
        }
    }


    private void handleKeyAction(KeyEvent e) {
        execute(FxActions.of(e));
    }


    private void handleDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
    }


    private void handleDragDropped(DragEvent e) {
        Dragboard board = e.getDragboard();
        if (board.hasFiles()) {
            var path = board.getFiles().stream().map(File::toPath)
                    .filter(Files::isReadable).filter(Files::isRegularFile).findFirst();
            if (path.isPresent()) {
                if (!canDiscardCurrent()) return;
                e.setDropCompleted(true);
                open(path.get());
                draw();
                return;
            }
        }
        e.setDropCompleted(false);
    }


    private void handleVerticalScroll(ObservableValue<? extends Number> ob, Number o, Number n) {
        model.scrollAt(n.intValue());
        draw();
    }


    private void handleHorizontalScroll(ObservableValue<? extends Number> ob, Number o, Number n) {
        model.scrollX(n.doubleValue());
        draw();
    }


    private void handleInputMethodTextChanged(InputMethodEvent e) {
        if (!e.getCommitted().isEmpty()) {
            model.imeOff();
            execute(Action.of(Action.Type.TYPED, e.getCommitted()));
        } else if (!e.getComposed().isEmpty()) {
            if (!model.isImeOn()) model.imeOn();
            model.inputImeComposed(e.getComposed().stream()
                    .map(InputMethodTextRun::getText)
                    .collect(Collectors.joining()));
            model.draw(draw);
        } else {
            model.imeOff();
        }
        draw();
    }


    private Action execute(Action action) {
        if (model.isImeOn()) return Action.EMPTY;

        switch (action.type()) {
            case TYPED -> model.input(action.attr());
            case DELETE -> model.delete();
            case BACK_SPACE -> model.backspace();
            case UNDO -> model.undo();
            case REDO -> model.redo();
            case HOME -> model.moveCaretHome(false);
            case END -> model.moveCaretEnd(false);
            case CARET_RIGHT -> model.moveCaretRight(false);
            case CARET_LEFT -> model.moveCaretLeft(false);
            case CARET_UP -> model.moveCaretUp(false);
            case CARET_DOWN -> model.moveCaretDown(false);
            case SELECT_CARET_RIGHT -> model.moveCaretRight(true);
            case SELECT_CARET_LEFT -> model.moveCaretLeft(true);
            case SELECT_CARET_UP -> model.moveCaretUp(true);
            case SELECT_CARET_DOWN -> model.moveCaretDown(true);
            case PAGE_UP -> model.moveCaretPageUp(false);
            case PAGE_DOWN -> model.moveCaretPageDown(false);
            case SELECT_PAGE_UP -> model.moveCaretPageUp(true);
            case SELECT_PAGE_DOWN -> model.moveCaretPageDown(true);
            case COPY -> model.copyToClipboard();
            case CUT -> model.cutToClipboard();
            case PASTE -> model.pasteFromClipboard();
            case ESC -> model.escape();
            case OPEN -> openWithChooser();
            case SAVE -> save();
            case SAVE_AS -> saveAs();
            case NEW -> newEdit();
        }
        if (action.type().syncCaret()) {
            model.scrollToCaret();
        }
        draw();
        return action;
    }


    private ScreenScroll screenScroll() {
        return new ScreenScroll() {
            @Override
            public void vertical(int min, int max, int val, int len) {
                vScroll.setMin(min);
                vScroll.setMax(max);
                vScroll.setValue(val);
                vScroll.setVisibleAmount(len);
            }
            @Override
            public void horizontal(double min, double max, double val, double len) {
                hScroll.setMin(min);
                hScroll.setMax(max);
                hScroll.setValue(val);
                hScroll.setVisibleAmount(len);
                hScroll.setVisible(max > len);
            }
            @Override
            public double xVal() {
                return hScroll.getValue();
            }
        };
    }


    private void draw() {
        model.draw(draw);
    }


    private void openWithChooser() {
        if (!canDiscardCurrent()) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        if (model.path().isPresent()) {
            fc.setInitialDirectory(
                    model.path().get().toAbsolutePath().getParent().toFile());
        } else {
            fc.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
        }
        File file = fc.showOpenDialog(getScene().getWindow());
        if (file != null) open(file.toPath());
    }


    private void open(Path path) {
        model = EditorModel.of(path, draw.fontMetrics(), screenScroll());
        model.setSize(getWidth(), getHeight());
    }


    private boolean canDiscardCurrent() {
        if (model.isModified()) {
            var result = FxDialog.confirmation(getScene().getWindow(),
                    "Are you sure you want to discard your changes?").showAndWait();
            return (result.isPresent() && result.get() == ButtonType.OK);
        } else {
            return true;
        }
    }


    private void save() {
        if (model.path().isPresent()) {
            model.save(model.path().get());
        } else {
            saveAs();
        }
    }


    private void saveAs() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save As...");
        fc.setInitialDirectory(model.path().isPresent()
                ? model.path().get().toAbsolutePath().getParent().toFile()
                : Path.of(System.getProperty("user.home")).toFile());
        File file = fc.showSaveDialog(getScene().getWindow());
        if (file != null) model.save(file.toPath());
    }


    private void newEdit() {
        Stage current = (Stage) getScene().getWindow();
        Stage stage = new Stage();
        stage.setX(current.getX() + (current.isFullScreen() ? 0 : 15));
        stage.setY(current.getY() + (current.isFullScreen() ? 0 : 15));
        var editorPane = new EditorPane();
        Scene scene = new Scene(editorPane, current.getWidth(), current.getHeight());
        scene.getStylesheets().addAll(getScene().getStylesheets());
        stage.setScene(scene);
        stage.setTitle("min-editor");
        stage.show();
    }


    private InputMethodRequests inputMethodRequests() {
        return new InputMethodRequests() {
            @Override
            public Point2D getTextLocation(int i) {
                return model.imeOn()
                        .map(loc -> canvas.localToScreen(loc.x(), loc.y()))
                        .orElse(null);
            }
            @Override
            public void cancelLatestCommittedText() { model.imeOff(); }
            @Override
            public int getLocationOffset(int x, int y) { return 0; }
            @Override
            public String getSelectedText() { return ""; }
        };
    }

}
