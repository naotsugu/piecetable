package com.mammb.code.piecetable.examples;

import com.mammb.code.piecetable.Document;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var editorPane = new EditorPane();
        Scene scene = new Scene(editorPane, 640, 480);
        scene.getStylesheets().add(String.join(",", "data:text/css;base64", css));
        stage.setScene(scene);
        stage.setTitle("editor");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    static class EditorPane extends StackPane {
        private final Canvas canvas;
        private final Draw draw;
        private ScreenText st;
        private final ScrollBar vs = new ScrollBar();
        private final ScrollBar hs = new ScrollBar();
        public EditorPane() {
            canvas = new Canvas(640, 480);
            canvas.setCursor(Cursor.TEXT);
            canvas.setFocusTraversable(true);
            getChildren().add(canvas);

            draw = new Draw.FxDraw(canvas.getGraphicsContext2D());
            st = ScreenText.of(Document.of(), draw.fontMetrics(), Syntax.of("java"));

            vs.setCursor(Cursor.DEFAULT);
            vs.setOrientation(Orientation.VERTICAL);
            vs.setMin(0);
            StackPane.setAlignment(vs, Pos.TOP_RIGHT);
            vs.valueProperty().addListener((ob, o, n) -> {
                if (st.getScrolledLineValue() != n.intValue()) {
                    st.scrollAt(n.intValue());
                    st.draw(draw);
                }
            });
            hs.setCursor(Cursor.DEFAULT);
            hs.setOrientation(Orientation.HORIZONTAL);
            StackPane.setAlignment(hs, Pos.BOTTOM_LEFT);
            hs.setMin(0);
            hs.valueProperty().addListener((ob, o, n) -> {
                if (st.getScrolledXValue() != n.doubleValue()) {
                    st.scrollX(n.doubleValue());
                    st.draw(draw);
                }
            });
            getChildren().addAll(vs, hs);

            layoutBoundsProperty().addListener((ob, o, n) -> {
                canvas.setWidth(n.getWidth());
                canvas.setHeight(n.getHeight());
                st.setSize(n.getWidth(), n.getHeight());
                draw();
            });
            setOnScroll((ScrollEvent e) -> {
                if (e.getEventType() == ScrollEvent.SCROLL && e.getDeltaY() != 0) {
                    if (e.getDeltaY() < 0) {
                        st.scrollNext((int) Math.min(5, Math.abs(e.getDeltaY())));
                    } else {
                        st.scrollPrev((int) Math.min(5, e.getDeltaY()));
                    }
                    draw();
                }
            });
            setOnMouseClicked((MouseEvent e) -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getTarget() == canvas) {
                    switch (e.getClickCount()) {
                        case 1 -> st.click(e.getX(), e.getY());
                        case 2 -> st.clickDouble(e.getX(), e.getY());
                        case 3 -> st.clickTriple(e.getX(), e.getY());
                    }
                    draw();
                }
            });
            setOnMouseDragged((MouseEvent e) -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    st.moveDragged(e.getX(), e.getY());
                    st.draw(draw);
                }
            });
            setOnKeyPressed((KeyEvent e) -> execute(st, Action.of(e)));
            setOnKeyTyped((KeyEvent e) -> execute(st, Action.of(e)));

            setOnDragOver((DragEvent e) -> {
                if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.MOVE);
            });
            setOnDragDropped((DragEvent e) -> {
                Dragboard board = e.getDragboard();
                if (board.hasFiles()) {
                    var path = board.getFiles().stream().map(File::toPath)
                        .filter(Files::isReadable).filter(Files::isRegularFile).findFirst();
                    if (path.isPresent()) {
                        open(path.get());
                        e.setDropCompleted(true);
                        return;
                    }
                }
                e.setDropCompleted(false);
            });

        }

        private Action execute(ScreenText st, Action action) {
            switch (action.type()) {
                case TYPED              -> { st.input(action.attr()); draw(); }
                case DELETE             -> { st.delete(); draw(); }
                case BACK_SPACE         -> { st.backspace(); draw(); }
                case CARET_RIGHT        -> { st.moveCaretRight(); draw(); }
                case CARET_LEFT         -> { st.moveCaretLeft(); draw(); }
                case CARET_DOWN         -> { st.moveCaretDown(); draw(); }
                case CARET_UP           -> { st.moveCaretUp(); draw(); }
                case HOME               -> { st.moveCaretHome(); draw(); }
                case END                -> { st.moveCaretEnd(); draw(); }
                case PAGE_UP            -> { st.moveCaretPageUp(); draw(); }
                case PAGE_DOWN          -> { st.moveCaretPageDown(); draw(); }
                case SELECT_CARET_RIGHT -> { st.moveCaretSelectRight(); draw(); }
                case SELECT_CARET_LEFT  -> { st.moveCaretSelectLeft(); draw(); }
                case SELECT_CARET_DOWN  -> { st.moveCaretSelectDown(); draw(); }
                case SELECT_CARET_UP    -> { st.moveCaretSelectUp(); draw(); }
                case SELECT_HOME        -> { st.moveCaretSelectHome(); draw(); }
                case SELECT_END         -> { st.moveCaretSelectEnd(); draw(); }
                case SELECT_PAGE_UP     -> { st.moveCaretSelectPageUp(); draw(); }
                case SELECT_PAGE_DOWN   -> { st.moveCaretSelectPageDown(); draw(); }
                case UNDO               -> { st.undo(); draw(); }
                case REDO               -> { st.redo(); draw(); }
                case COPY               -> { st.copyToClipboard(); }
                case PASTE              -> { st.pasteFromClipboard(); draw(); }
                case CUT                -> { st.cutToClipboard(); draw(); }
                case OPEN               -> open();
                case SAVE               -> save();
                case SAVE_AS            -> saveAs();
                case NEW                -> openNew();
            }
            return action;
        }

        private void open() {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select file...");
            fc.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
            File file = fc.showOpenDialog(getScene().getWindow());
            if (file != null) open(file.toPath());
        }
        private void open(Path path) {
            String ext = Optional.of(path.getFileName().toString())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1))
                .orElse("");
            st = ScreenText.of(Document.of(path), draw.fontMetrics(), Syntax.of(ext));
            st.setSize(getWidth(), getHeight());
            draw();
        }
        private void save() {
            if (st.path() == null) {
                saveAs();
            } else {
                st.save(st.path());
            }
        }
        private void saveAs() {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save As...");
            fc.setInitialDirectory((st.path() == null)
                ? Path.of(System.getProperty("user.home")).toFile()
                : st.path().getParent().toFile());
            File file = fc.showSaveDialog(getScene().getWindow());
            st.save(file.toPath());
        }
        private void openNew() {
            st = ScreenText.of(Document.of(), draw.fontMetrics(), Syntax.of(""));
            st.setSize(getWidth(), getHeight());
            draw();
        }

        private void draw() {
            st.draw(draw);
            vs.setMax(st.getScrollableMaxLine());
            vs.setValue(st.getScrolledLineValue());
            vs.setVisibleAmount(st.screenLineSize());
            if (st.getScrollableMaxX() > 0) {
                hs.setMax(st.getScrollableMaxX());
                hs.setPrefWidth(getWidth() - vs.getWidth());
                hs.setVisibleAmount((canvas.getWidth() - vs.getWidth()) *
                    (canvas.getWidth() / (st.getScrollableMaxX() + canvas.getWidth())));
                hs.setVisible(true);
            } else {
                hs.setVisible(false);
            }
        }
    }

    private static String css = Base64.getEncoder().encodeToString("""
            .root {
              -fx-base:app-base;
              -fx-accent:app-accent;
              -fx-background:-fx-base;
              -fx-control-inner-background:app-back;
              -fx-control-inner-background-alt: derive(-fx-control-inner-background,-2%);
              -fx-focus-color: -fx-accent;
              -fx-faint-focus-color:app-accent22;
              -fx-light-text-color:app-text;
              -fx-mark-color: -fx-light-text-color;
              -fx-mark-highlight-color: derive(-fx-mark-color,20%);
              -fx-background-color:app-back;
            }
            .text-input, .label {
              -fx-font: 14px "Consolas";
            }
            .menu-bar {
              -fx-use-system-menu-bar:true;
              -fx-background-color:derive(-fx-control-inner-background,20%);
            }
            """
        .replaceAll("app-base", Draw.base)
        .replaceAll("app-text", Draw.text)
        .replaceAll("app-back", Draw.back)
        .replaceAll("app-accent", Draw.accent)
        .getBytes(StandardCharsets.UTF_8));

}
