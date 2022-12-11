package com.mammb.code.editor;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.AccessibleRole;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class TextPane extends Region {

    private static final Color bgColor = Color.web("#303841");
    private static final Color fgColor = Color.web("#d3dee9");
    private static final Font font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);
    private static final double lineHeight = Utils.getTextHeight(font);

    private ScreenBuffer screenBuffer = new ScreenBuffer();
    private Stage stage;
    private TextFlow textFlow;
    private Caret caret;

    public TextPane(Stage stage) {

        this.stage = stage;

        setBackground(new Background(new BackgroundFill(bgColor, null, null)));
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setOnKeyPressed(this::handleKeyPressed);
        setOnScroll(this::handleScroll);

        textFlow = new TextFlow();
        textFlow.setTabSize(4);
        textFlow.setPadding(new Insets(4));
        getChildren().add(textFlow);
        textFlow.getChildren().addAll(texts());

        caret = new Caret();
        caret.setLayoutX(textFlow.getPadding().getLeft());
        caret.setLayoutY(textFlow.getPadding().getTop());
        screenBuffer.caretOffsetProperty().addListener(this::handleCaretMoved);
        screenBuffer.addListChangeListener(this::handleScreenTextChanged);
        getChildren().add(caret);
        caret.setShape(textFlow.caretShape(0, true));

        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.getHeight() != newValue.getHeight()) {
                screenBuffer.setRowSize((int) Math.ceil(newValue.getHeight() / lineHeight));
            }
        });

    }


    void handleScreenTextChanged(ListChangeListener.Change<? extends String> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                textFlow.getChildren().addAll(change.getFrom(), change.getAddedSubList().stream().map(this::asText).toList());
            } else if (change.wasRemoved()) {
                textFlow.getChildren().remove(change.getFrom(), Math.max(change.getTo(), change.getFrom() + 1));
            }
        }
    }


    void handleCaretMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (newValue.intValue() < 0 || newValue.intValue() > screenBuffer.getTextLengthOnScreen() ) {
            caret.disable();
        } else {
            caret.setShape(textFlow.caretShape(newValue.intValue(), true));
        }
    }


    public void open(File file) {
        screenBuffer.open(file.toPath());
        textFlow.getChildren().setAll(texts());
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

    private void handleScroll(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL) {
            if (e.getDeltaY() > 2)  screenBuffer.scrollUp(2);
            else if (e.getDeltaY() > 0)  screenBuffer.scrollUp(1);
            else if (e.getDeltaY() < -2) screenBuffer.scrollDown(2);
            else if (e.getDeltaY() < 0)  screenBuffer.scrollDown(1);
        }

    }

    private static File fileChooseOpen(Window owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        return fc.showOpenDialog(owner);
    }


    private static final KeyCombination SC_O = new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN);


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
