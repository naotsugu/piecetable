package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ScreenBuffer {

    final ObservableList<String> rows = FXCollections.observableArrayList("");

    /** Caret row on the text flow. */
    private int caretRowOnScreen = 0;
    /** Caret offset on the row. May be larger than the number of characters in a row. */
    private int caretLogicalOffsetOnRow = 0;
    /** Offset on the text flow. */
    private IntegerProperty caretOffset = new SimpleIntegerProperty();
    /** rowSize. */
    private int rowSize = 1;

    private final Content content = new PtContent();
    private int headRowOnContent = 0;
    private int headPosOnContent = 0;
    private boolean dirty = false;


    public ScreenBuffer() {
    }


    public void open(Path path) {
        content.open(path);
        headRowOnContent = headPosOnContent = caretRowOnScreen = caretLogicalOffsetOnRow = 0;
        caretOffset.set(0);
        rows.clear();
        dirty = false;
        setRowSize(rowSize);
    }


    public void next() {
        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position
        caretLogicalOffsetOnRow++;
        if (caretLogicalOffsetOnRow > caretRowTextLength()) {
            caretLogicalOffsetOnRow = 0;
            caretRowOnScreen++;
        }
        caretOffset(+1);
    }


    public void prev() {
        if (headRowOnContent == 0 && caretRowOnScreen == 0 && getCaretOffset() == 0) {
            return;
        }
        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position

        if (caretRowOnScreen > 0 && caretLogicalOffsetOnRow == 0) {
            caretRowOnScreen--;
            caretLogicalOffsetOnRow = caretRowTextLength();
        } else {
            caretLogicalOffsetOnRow--;
        }
        caretOffset(-1);
    }


    public void nextLine() {

        if (caretPosOnContent() + rows.get(caretRowOnScreen).length() > content.length()) {
            return;
        }

        int remaining = rows.get(caretRowOnScreen).length() - caretOffsetOnRow();
        caretRowOnScreen++;
        caretOffset(remaining + caretOffsetOnRow());
    }


    public void prevLine() {

        if (headRowOnContent == 0 && caretRowOnScreen == 0) {
            return;
        }

        int remaining = caretOffsetOnRow();
        caretRowOnScreen--;
        caretOffset(- remaining - (rows.get(caretRowOnScreen).length() - caretOffsetOnRow()));
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


    public void scrollUp(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (headRowOnContent == 0) return;

            String firstRow = content.untilSol(headPosOnContent - 1);
            rows.add(0, firstRow);
            if (rows.size() > rowSize) {
                rows.remove(rows.size() - 1);
            }
            headRowOnContent--;
            headPosOnContent -= firstRow.length();
        }
    }


    public void scrollDown(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (rows.size() < (int) Math.ceil(rowSize * 2.0 / 3.0)) return;

            int removeLineLength = rows.get(0).length();
            int bottomPosOnContent = bottomPosOnContent();
            if (bottomPosOnContent < content.length()) {
                rows.add(content.untilEol(bottomPosOnContent));
            }
            rows.remove(0);
            headRowOnContent++;
            headPosOnContent += removeLineLength;
        }
    }


    public int caretRowOnContent() {
        return headRowOnContent + caretRowOnScreen;
    }


    public int caretPosOnContent() {
        int count = 0;
        for (int i = 0; i < caretRowOnScreen; i++) {
            count += rows.get(i).length();
        }
        count += caretOffsetOnRow();
        return headPosOnContent + count;
    }


    public int bottomPosOnContent() {
        return headPosOnContent + rows.stream().mapToInt(String::length).sum();
    }

    public int caretOffsetOnRow() {
        return Math.min(caretRowTextLength(), caretLogicalOffsetOnRow);
    }

    public int caretRowTextLength() {
        String row = rows.get(caretRowOnScreen);
        return row.endsWith("\n") ? row.length() - 1 : row.length();
    }

    void setRowSize(int preferenceSize) {
        rowSize = preferenceSize;
        if (preferenceSize == rows.size())  return;
        while (preferenceSize < rows.size()) {
            rows.remove(rows.size() - 1);
        }
        if (preferenceSize > rows.size()) {
            for (int i = bottomPosOnContent(); i < content.length() && preferenceSize > rows.size();) {
                String string = this.content.untilEol(i);
                i += (string.length() == 0) ? 1 : string.length();
                rows.add(string);
            }
        }
    }

    void addListChangeListener(ListChangeListener<String> listener) {
        rows.addListener(listener);
    }

    private void caretOffset(int delta) { setCaretOffset(getCaretOffset() + delta); }
    public final int getCaretOffset() { return caretOffset.get(); }
    public final void setCaretOffset(int value) { caretOffset.set(value); }
    public IntegerProperty caretOffsetProperty() { return caretOffset; }


}
