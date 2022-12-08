package com.mammb.code.editor;

import javafx.geometry.Insets;
import javafx.scene.AccessibleRole;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;

public class TextPane extends Region {

    private static final Color bgColor = Color.web("#303841");
    private static final Color fgColor = Color.web("#d3dee9");
    private static final Font font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);

    private ScreenBuffer screenBuffer = new ScreenBuffer();
    private Stage stage;
    private TextFlow textFlow;
    private Content content;

    public TextPane(Stage stage) {

        this.stage = stage;

        setBackground(new Background(new BackgroundFill(bgColor, null, null)));
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setOnKeyPressed(this::handleKeyPressed);

        textFlow = new TextFlow();
        textFlow.setTabSize(4);
        textFlow.setPadding(new Insets(4));
        getChildren().add(textFlow);
        textFlow.getChildren().addAll(screenBuffer.texts());

        Caret caret = new Caret();
        caret.setLayoutX(textFlow.getPadding().getLeft());
        caret.setLayoutY(textFlow.getPadding().getTop());
        screenBuffer.caretOffsetProperty().addListener((observable, oldValue, newValue) ->
            caret.setShape(textFlow.caretShape(newValue.intValue(), true)));
        getChildren().add(caret);
        caret.setShape(textFlow.caretShape(0, true));

    }

    public void open(File file) {
        screenBuffer.open(file.toPath());
        textFlow.getChildren().setAll(screenBuffer.texts());
    }

    private void handleKeyPressed(KeyEvent e) {
        if (SC_O.match(e)) {
            open(fileChooseOpen(stage));
            return;
        }

        switch (e.getCode()) {
            case LEFT  -> screenBuffer.prev();
            case RIGHT -> screenBuffer.next();
            case UP    -> screenBuffer.prevLine();
            case DOWN  -> screenBuffer.nextLine();
            case HOME  -> screenBuffer.home();
            case END   -> screenBuffer.end();
            default -> { }
        }
    }

    private static File fileChooseOpen(Window owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        return fc.showOpenDialog(owner);
    }


    private static final KeyCombination SC_O = new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN);

}
