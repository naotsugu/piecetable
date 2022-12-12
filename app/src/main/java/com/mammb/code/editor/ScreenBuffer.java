package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.ArrayList;
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

        if (caretPosOnContent() >= content.length()) {
            return;
        }

        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position
        caretLogicalOffsetOnRow++;
        if (caretLogicalOffsetOnRow > caretRowTextLength()) {
            caretLogicalOffsetOnRow = 0;
            caretRowOnScreen++;
        }
        moveCaretPositionDelta(+1);

        if (caretRowOnScreen + 2 > rowSize && bottomLengthOnContent() <= content.length()) {
            scrollDown(1);
        }

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
        moveCaretPositionDelta(-1);

        if (headRowOnContent > 0 && caretRowOnScreen - 1 < 0) {
            scrollUp(1);
        }
    }


    public void nextLine() {

        if (caretPosOnContent() + caretRemainingOnRow() >= content.length() - 1) {
            end();
            next();
            return;
        }

        int remaining = rows.get(caretRowOnScreen).length() - caretOffsetOnRow();
        caretRowOnScreen++;
        moveCaretPositionDelta(remaining + caretOffsetOnRow());

        if (caretRowOnScreen + 2 > rowSize && bottomLengthOnContent() <= content.length()) {
            scrollDown(1);
        }
    }


    public void prevLine() {

        if (headRowOnContent == 0 && caretRowOnScreen == 0) {
            return;
        }

        int remaining = caretOffsetOnRow();
        caretRowOnScreen--;
        moveCaretPositionDelta(- remaining - (rows.get(caretRowOnScreen).length() - caretOffsetOnRow()));

        if (headRowOnContent > 0 && caretRowOnScreen - 1 < 0) {
            scrollUp(1);
        }
    }


    public void home() {
        int remaining = caretOffsetOnRow();
        caretLogicalOffsetOnRow = 0;
        moveCaretPositionDelta(-remaining);
    }


    public void end() {
        int remaining = caretRowTextLength() - caretOffsetOnRow();
        caretLogicalOffsetOnRow = caretRowTextLength();
        moveCaretPositionDelta(+remaining);
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

            caretRowOnScreen++;
            moveCaretPositionDelta(firstRow.length());

        }
    }


    public void scrollDown(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (rows.size() < (int) Math.ceil(rowSize * 2.0 / 3.0)) return;

            int removeLineLength = rows.get(0).length();
            int bottomLengthOnContent = bottomLengthOnContent();
            if (bottomLengthOnContent < content.length()) {
                rows.add(content.untilEol(bottomLengthOnContent));
            }
            rows.remove(0);
            headRowOnContent++;
            headPosOnContent += removeLineLength;

            caretRowOnScreen--;
            moveCaretPositionDelta(-removeLineLength);
        }
    }


    void locateScreenToCaretScope() {
        if (caretOffset.get() < 0) {

        } else if (headPosOnContent + caretOffset.get() > bottomLengthOnContent()) {

        }

    }


    /**
     * <pre>
     *    |x|x|↵  0
     *    |x|x|↵  1
     * ---------
     * 0: |a|b|↵  2  ← headRowOnContent 2
     * 1: |c|d|                         ↓
     * ↑ caretRowOnScreen 1  → caretRowOnContent 3
     * </pre>
     */
    public int caretRowOnContent() {
        return headRowOnContent + caretRowOnScreen;
    }


    /**
     * TODO
     * <pre>
     * |x|x|↵
     * ---------
     *  ↓ headPosOnContent 3
     * |a|b|↵              ↓
     * |c|d|               ↓
     *   ↑ caretOffset 4 → caretPosOnContent 7
     * </pre>
     */
    public int caretPosOnContent() {
        return headPosOnContent + caretOffset.get();
    }


    /**
     * <pre>
     * |x|x|↵
     * ---------
     *  ↓ headPosOnContent 3
     * |a|b|↵  3            ↓
     * |c|d|   2           bottomLengthOnContent 8
     *       -----          ↑
     *         5 getTextLengthOnScreen
     * </pre>
     */
    public int bottomLengthOnContent() {
        return headPosOnContent + textLengthOnScreen();
    }


    /**
     * <pre>
     * |c|o|n|t|e|n|t|↵
     *       ↑ caretLogicalOffsetOnRow
     *       ↑ caretOffsetOnRow
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|↵
     *               ↑      ↑ caretLogicalOffsetOnRow
     *               └ caretOffsetOnRow
     * </pre>
     */
    public int caretOffsetOnRow() {
        return Math.min(caretRowTextLength(), caretLogicalOffsetOnRow);
    }

    /**
     * <pre>
     * |c|o|n|t|e|n|t|↵
     *         ↑     ↑ caretRowTextLength
     *         └ caretOffsetOnRow
     *         │← 3 →│ caretRemainingOnRow
     * </pre>
     */
    public int caretRemainingOnRow() {
        return caretRowTextLength() - caretOffsetOnRow();
    }


    /**
     * <pre>
     * |c|o|n|t|e|n|t|↵
     * │←     7     →│ caretRowTextLength
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|
     * │←     7     →│ caretRowTextLength
     * </pre>
     */
    public int caretRowTextLength() {
        if (caretRowOnScreen >= rows.size() || caretRowOnScreen < 0) {
            return 0;
        }
        String row = rows.get(caretRowOnScreen);
        return row.endsWith("\n") ? row.length() - 1 : row.length();
    }


    /**
     * <pre>
     * |a|b|↵  3
     * |c|d|   2
     *       -----
     *         5 getTextLengthOnScreen
     * </pre>
     */
    int textLengthOnScreen() {
        return rows.stream().mapToInt(String::length).sum();
    }


    void setRowSize(int preferenceSize) {
        rowSize = preferenceSize;
        if (preferenceSize == rows.size())  return;
        while (preferenceSize < rows.size()) {
            rows.remove(rows.size() - 1);
        }
        if (preferenceSize > rows.size()) {
            for (int i = bottomLengthOnContent(); i < content.length() && preferenceSize > rows.size();) {
                String string = this.content.untilEol(i);
                i += (string.length() == 0) ? 1 : string.length();
                rows.add(string);
            }
        }
    }

    public final void moveCaretOffset(int offset) {
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            int len = rows.get(i).length();
            index += len;
            if (index > offset) {
                caretRowOnScreen = i;
                caretLogicalOffsetOnRow = len - (index - offset);
                break;
            }
        }
        caretOffset.set(offset);
    }

    void addListChangeListener(ListChangeListener<String> listener) {
        rows.addListener(listener);
    }

    private void moveCaretPositionDelta(int delta) {
        setCaretOffset(getCaretOffset() + delta);
    }


    public final int getCaretOffset() { return caretOffset.get(); }
    public final void setCaretOffset(int value) { caretOffset.set(value); }
    public IntegerProperty caretOffsetProperty() { return caretOffset; }

}
