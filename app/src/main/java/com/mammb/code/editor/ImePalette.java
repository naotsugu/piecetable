package com.mammb.code.editor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ImePalette extends Region {

    private final BooleanProperty imeOn = new SimpleBooleanProperty(false);
    private Text palette;
    private Text original;
    private final TextFlow paletteFlow;

    private final TextFlow parent;
    private final ScreenBuffer screenBuffer;


    public ImePalette(TextFlow parent, ScreenBuffer screenBuffer) {

        setVisible(false);

        this.parent = parent;
        this.screenBuffer = screenBuffer;

        this.palette = new Text();
        this.palette.setFont(Fonts.main);
        this.palette.setFill(Colors.fgColor);
        this.palette.setUnderline(true);

        this.original = new Text();
        this.original.setFont(Fonts.main);
        this.original.setFill(Colors.fgColor);

        this.paletteFlow = new TextFlow(palette, original);
        this.paletteFlow.setTabSize(4);
        this.paletteFlow.setBackground(new Background(new BackgroundFill(Colors.bgColor, null, null)));

        getChildren().add(paletteFlow);

    }


    void handleInputMethod(InputMethodEvent e) {
        if (!getImeOn()) {
            setImeOn(true);
            original.setText(screenBuffer.caretLineText().substring(screenBuffer.caretLineCharOffset()));
        }
        if (e.getCommitted().length() > 0) {
            screenBuffer.add(e.getCommitted());
            clear();
        } else if (!e.getComposed().isEmpty()) {
            setVisible(true);
            Point2D point = top();
            setLayoutX(point.getX() + 4);
            setLayoutY(point.getY() + 4);
            palette.setText(e.getComposed().stream()
                .map(InputMethodTextRun::getText).collect(Collectors.joining()));
        }
        if (e.getCommitted().length() == 0 && e.getComposed().isEmpty()) {
            clear();
        }
    }


    public InputMethodRequests createInputMethodRequests() {
        return new InputMethodRequests() {
            @Override
            public Point2D getTextLocation(int offset) {
                Bounds bounds = localToScreen(getParent().getParent().getBoundsInParent());
                return bottom().add(bounds.getMinX(), bounds.getMinY());
            }
            @Override
            public int getLocationOffset(int x, int y) {
                return 0;
            }
            @Override
            public void cancelLatestCommittedText() {
                imeOn.set(false);
            }
            @Override
            public String getSelectedText() {
                return "";
            }
        };
    }

    private Point2D top() {
        PathElement[] paths = parent.caretShape(screenBuffer.getCaretOffset(), true);
        return (paths[0] instanceof MoveTo moveTo)
            ? new Point2D(moveTo.getX(), moveTo.getY())
            : new Point2D(0, 0);
    }

    private Point2D bottom() {
        PathElement[] paths = parent.caretShape(screenBuffer.getCaretOffset(), true);
        return (paths.length > 1 && paths[1] instanceof LineTo lineTo)
            ? new Point2D(lineTo.getX(), lineTo.getY())
            : new Point2D(0, 0);
    }


    private void clear() {
        setVisible(false);
        palette.setText("");
        original.setText("");
        imeOn.set(false);
    }


    public final boolean getImeOn() { return imeOn.get(); }
    void setImeOn(boolean value) { imeOn.set(value); }
    public BooleanProperty imeOnProperty() { return imeOn; }

}
