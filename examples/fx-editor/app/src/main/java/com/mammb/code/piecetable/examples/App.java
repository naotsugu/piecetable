package com.mammb.code.piecetable.examples;

import com.mammb.code.piecetable.Document;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var editorPane = new EditorPane();
        Scene scene = new Scene(new VBox(editorPane.menuBar(), editorPane), 640, 480);
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
            var doc = Document.of(Path.of("build.gradle.kts"));
            st = ScreenText.of(doc, draw.fontMetrics(), Syntax.of("java"));

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
            setOnKeyPressed((KeyEvent e) -> execute(st, Action.of(e)));
            setOnKeyTyped((KeyEvent e) -> execute(st, Action.of(e)));

        }
        MenuBar menuBar() {
            MenuBar menuBar = new MenuBar();
            Menu menuFile = new Menu("File");
            MenuItem menuOpen = new MenuItem("Open");
            MenuItem menuSave = new MenuItem("Save");
            MenuItem menuSaveAs = new MenuItem("Save As");
            MenuItem menuQuit = new MenuItem("Quit");
            menuFile.getItems().addAll(menuOpen, menuSave, menuSaveAs, menuQuit);
            Menu menuEdit = new Menu("Edit");
            MenuItem menuCut = new MenuItem("Cut");
            MenuItem menuCopy = new MenuItem("Copy");
            MenuItem menuPaste = new MenuItem("Paste");
            menuEdit.getItems().addAll(menuCut, menuCopy, menuPaste);
            menuBar.getMenus().addAll(menuFile, menuEdit);

            menuOpen.setOnAction(e -> {  });
            menuSave.setOnAction(e -> {  });
            menuSaveAs.setOnAction(e -> {  });
            menuQuit.setOnAction(e -> { Platform.exit(); });

            menuCut.setOnAction(e -> {  });
            menuCopy.setOnAction(e -> {  });
            menuPaste.setOnAction(e -> {  });

            return menuBar;
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
            }
            return action;
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
