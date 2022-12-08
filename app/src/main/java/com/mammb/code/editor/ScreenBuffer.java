package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ScreenBuffer {

    private final List<String> rows = new LinkedList<>();

    /** Caret row on the text flow. */
    private int caretRow = 0;
    /** Caret offset on the row. May be larger than the number of characters in a row. */
    private int caretLogicalOffsetOnRow = 0;
    /** Offset on the text flow. */
    private IntegerProperty caretOffset = new SimpleIntegerProperty();

    private final Content content = new PtContent();
    private int headRow = 0;
    private int headPos = 0;
    private boolean dirty = false;


    public ScreenBuffer() {
        rows.add("");
    }

    public void open(Path path) {

        content.open(path);
        headRow = headPos = caretRow = caretLogicalOffsetOnRow = 0;
        caretOffset.set(0);
        rows.clear();
        dirty = false;

        for (int i = 0; i < content.length();) {
            String string = this.content.untilEol(i);
            i += (string.length() == 0) ? 1 : string.length();
            rows.add(string);
        }
    }


    private String contentRowPrev() {
        if (headPos > 0) {
            String string = content.untilEol(headPos - 1);
            headPos -= string.length();
            headRow--;
            return string;
        }
        return null;
    }

    private String contentRowNext() {
        int bottom = bottomPosOnContent();
        if (bottom < content.length()) {
            String string = content.untilEol(bottom + 1);
            return string;
        }
        return null;
    }

    private static final Color bgColor = Color.web("#303841");
    private static final Color fgColor = Color.web("#d3dee9");
    private static final Font font = Font.font("Consolas", FontWeight.NORMAL, FontPosture.REGULAR, 16);
    List<Text> texts() {
        List<Text> list = new ArrayList<>();
        for (String string : rows) {
            Text text = new Text(string);
            text.setFont(font);
            text.setFill(fgColor);
            list.add(text);
        }
        return list;
    }


    public void next() {
        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position
        caretLogicalOffsetOnRow++;
        if (caretLogicalOffsetOnRow > caretRowTextLength()) {
            caretLogicalOffsetOnRow = 0;
            caretRow++;
        }
        caretOffset(+1);
    }


    public void prev() {
        if (headRow == 0 && caretRow == 0 && getCaretOffset() == 0) {
            return;
        }
        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position

        if (caretRow > 0 && caretLogicalOffsetOnRow == 0) {
            caretRow--;
            caretLogicalOffsetOnRow = caretRowTextLength();
        } else {
            caretLogicalOffsetOnRow--;
        }
        caretOffset(-1);
    }


    public void nextLine() {
        int remaining = rows.get(caretRow).length() - caretOffsetOnRow();
        caretRow++;
        caretOffset(remaining + caretOffsetOnRow());
    }


    public void prevLine() {
        if (headRow == 0 && caretRow == 0) return;
        int remaining = caretOffsetOnRow();
        caretRow--;
        caretOffset(- remaining - (rows.get(caretRow).length() - caretOffsetOnRow()));
    }

    public void home() {
        int remaining = caretOffsetOnRow();
        caretLogicalOffsetOnRow = 0;
        caretOffset(- remaining);
    }

    public void end() {
        int remaining = caretRowTextLength() - caretOffsetOnRow();
        caretLogicalOffsetOnRow = caretRowTextLength();
        caretOffset(+ remaining);
    }

    public void scrollUp() {

    }

    public void scrollDown() {

    }

    public int caretRowOnContent() {
        return headRow + caretRow;
    }


    public int caretPosOnContent() {
        int count = 0;
        for (int i = 0; i < caretRow; i++) {
            count += rows.get(i).length();
        }
        count += caretOffsetOnRow();
        return headPos + count;
    }


    public int bottomPosOnContent() {
        return headPos + rows.stream().mapToInt(String::length).sum();
    }

    public int caretOffsetOnRow() {
        return Math.min(caretRowTextLength(), caretLogicalOffsetOnRow);
    }

    public int caretRowTextLength() {
        String row = rows.get(caretRow);
        return row.endsWith("\n") ? row.length() - 1 : row.length();
    }

    private void caretOffset(int delta) { setCaretOffset(getCaretOffset() + delta); }
    public final int getCaretOffset() { return caretOffset.get(); }
    public final void setCaretOffset(int value) { caretOffset.set(value); }
    public IntegerProperty caretOffsetProperty() { return caretOffset; }

}
