package com.mammb.code.editor;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleRole;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextPane extends Region {

    private static final Color bgColor = Color.web("#303841");
    private static final Color fgColor = Color.web("#d3dee9");
    private static final Font font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);
    private static final double lineHeight = Utils.getTextHeight(font);

    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ScreenBuffer screenBuffer = new ScreenBuffer();
    private final Stage stage;
    private final TextFlow textFlow;
    private final Caret caret;

    public TextPane(Stage stage) {

        this.stage = stage;

        setBackground(new Background(new BackgroundFill(bgColor, null, null)));
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setOnKeyPressed(this::handleKeyPressed);
        setOnScroll(this::handleScroll);
        setOnMouseClicked(this::handleMouseClicked);
        setOnKeyTyped(this::handleInput);

        textFlow = new TextFlow();
        textFlow.setTabSize(4);
        textFlow.setPrefWidth(Screen.getPrimary().getBounds().getWidth());
        textFlow.setPadding(new Insets(4));
        //getChildren().add(textFlow);
        textFlow.getChildren().addAll(texts());

        caret = new Caret();
        caret.setLayoutX(textFlow.getPadding().getLeft());
        caret.setLayoutY(textFlow.getPadding().getTop());
        screenBuffer.caretOffsetProperty().addListener(this::handleCaretMoved);
        screenBuffer.addListChangeListener(this::handleScreenTextChanged);
        //getChildren().add(caret);
        caret.setShape(textFlow.caretShape(0, true));


        BorderPane pane = new BorderPane();
        Pane main = new Pane(textFlow, caret);
        Pane left = new SidePanel(screenBuffer);
        left.setPadding(new Insets(4));
        pane.setLeft(left);
        pane.setCenter(main);
        getChildren().add(pane);

        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.getHeight() != newValue.getHeight()) {
                screenBuffer.setupScreenRowSize((int) Math.ceil(newValue.getHeight() / lineHeight));
            }
        });

    }


    void handleScreenTextChanged(ListChangeListener.Change<? extends String> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                textFlow.getChildren().remove(change.getFrom(), Math.max(change.getTo(), change.getFrom() + 1));
            }
            if (change.wasAdded()) {
                textFlow.getChildren().addAll(change.getFrom(),
                    change.getAddedSubList().stream().map(this::asText).toList());
            }
        }
    }


    void handleCaretMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (newValue.intValue() < 0 || newValue.intValue() > screenBuffer.charCountOnScreen() ) {
            caret.disable();
        } else {
            caret.setShape(textFlow.caretShape(newValue.intValue(), true));
        }
    }

    private void handleInput(KeyEvent e) {

        if (e.getCode().isFunctionKey() || e.getCode().isNavigationKey() ||
            e.getCode().isArrowKey() || e.getCode().isModifierKey() ||
            e.getCode().isMediaKey() || !controlKeysFilter.test(e) ||
            e.getCharacter().length() == 0) {
            return;
        }
        int ascii = e.getCharacter().getBytes()[0];
        if (ascii < 32 || ascii == 127) { // 127:DEL
            if (ascii != 10 && ascii != 13) { // 10:LF 13:CR
                return;
            }
        }
        // Enter key : 13:CR -> replace to 10:LF
        screenBuffer.add(e.getCharacter().replace('\r', '\n'));
    }

    private void handleKeyPressed(KeyEvent e) {
        if (SC_O.match(e)) {
            File file = fileChooseOpen(stage);
            if (file != null) screenBuffer.open(file.toPath());
            return;
        }
        if (SC_S.match(e)) {
            if (screenBuffer.getPath() != null) {
                screenBuffer.save();
            } else {
                File file = fileChooseOpen(stage);
                if (file != null) screenBuffer.saveAs(file.toPath());
            }
            return;
        }
        if (SC_SA.match(e)) {
            File file = fileChooseOpen(stage);
            if (file != null) screenBuffer.saveAs(file.toPath());
            return;
        }
        if (SC_C.match(e)) {
            return;
        }
        if (SC_V.match(e)) {
            if (clipboard.hasString()) screenBuffer.add(clipboard.getString());
            return;
        }
        if (SC_X.match(e)) {
            return;
        }
        if (SC_Z.match(e)) {
            screenBuffer.undo();
            return;
        }
        if (SC_SZ.match(e)) {
            screenBuffer.redo();
            return;
        }

        switch (e.getCode()) {
            case LEFT       -> screenBuffer.prev();
            case RIGHT      -> screenBuffer.next();
            case UP         -> screenBuffer.prevLine();
            case DOWN       -> screenBuffer.nextLine();
            case HOME       -> screenBuffer.home();
            case END        -> screenBuffer.end();
            case PAGE_UP    -> screenBuffer.pageUp();
            case PAGE_DOWN  -> screenBuffer.pageDown();
            case DELETE     -> screenBuffer.delete(1);
            case BACK_SPACE -> screenBuffer.backSpace();
            case F1         -> screenBuffer.inspect();
            case ESCAPE     -> screenBuffer.reset();
            default -> { }
        }
    }

    private void handleScroll(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL) {
            if (e.getDeltaY() > 2)  screenBuffer.scrollPrev(2);
            else if (e.getDeltaY() > 0)  screenBuffer.scrollPrev(1);
            else if (e.getDeltaY() < -2) screenBuffer.scrollNext(2);
            else if (e.getDeltaY() < 0)  screenBuffer.scrollNext(1);
        }
    }

    private void handleMouseClicked(MouseEvent e) {
        HitInfo hit = textFlow.hitTest(textFlow.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY())));
        if (e.getClickCount() == 1) {
            screenBuffer.moveCaret(hit.getInsertionIndex());
        } else if (e.getClickCount() == 2) {
            // word select
        }
    }


    private static File fileChooseOpen(Window owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        return fc.showOpenDialog(owner);
    }


    private static final KeyCombination SC_C = new KeyCharacterCombination("c", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_V = new KeyCharacterCombination("v", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_X = new KeyCharacterCombination("x", KeyCombination.SHORTCUT_DOWN);

    private static final KeyCombination SC_O = new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_S = new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_SA= new KeyCharacterCombination("s", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    private static final KeyCombination SC_Z = new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination SC_SZ= new KeyCharacterCombination("z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    private static final Predicate<KeyEvent> controlKeysFilter = e ->
        System.getProperty("os.name").startsWith("Windows")
            ? !e.isControlDown() && !e.isAltDown() && !e.isMetaDown() && e.getCharacter().length() == 1 && e.getCharacter().getBytes()[0] != 0
            : !e.isControlDown() && !e.isAltDown() && !e.isMetaDown();

    List<Text> texts() {
        return screenBuffer.rows.stream().map(this::asText).collect(Collectors.toList());
    }

    Text asText(String string) {
        Text text = new Text(string);
        text.setFont(font);
        text.setFill(fgColor);
        return text;
    }

}
