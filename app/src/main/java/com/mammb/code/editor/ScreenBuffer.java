package com.mammb.code.editor;

import java.nio.file.Path;
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

    private final Content content = new EditBufferedContent(new PtContent());
    private int headRowOnContent = 0;
    private int headPosOnContent = 0;


    public ScreenBuffer() {
    }


    public void open(Path path) {
        content.open(path);
        headRowOnContent = headPosOnContent = caretRowOnScreen = caretLogicalOffsetOnRow = 0;
        caretOffset.set(0);
        rows.clear();
        setRowSize(rowSize);
    }


    public void next() {

        locateToCaretScope();

        if (caretPosOnContent() >= content.length()) {
            return;
        }

        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position
        caretLogicalOffsetOnRow++;
        if (caretLogicalOffsetOnRow > caretRowTextLength()) {
            // move caret to the next line head
            caretLogicalOffsetOnRow = 0;
            caretRowOnScreen++;
        }
        setCaretOffset(getCaretOffset() + 1);

        if (caretRowOnScreen + 2 > rowSize && bottomLengthOnContent() <= content.length()) {
            scrollDown(1);
        }

    }


    public void prev() {

        locateToCaretScope();

        if (headRowOnContent == 0 && caretRowOnScreen == 0 && getCaretOffset() == 0) {
            return;
        }
        caretLogicalOffsetOnRow = caretOffsetOnRow(); // reset logical position

        if (caretRowOnScreen > 0 && caretLogicalOffsetOnRow == 0) {
            // move caret to the prev line tail
            caretRowOnScreen--;
            caretLogicalOffsetOnRow = caretRowTextLength();
        } else {
            caretLogicalOffsetOnRow--;
        }
        setCaretOffset(getCaretOffset() - 1);

        if (headRowOnContent > 0 && caretRowOnScreen - 1 < 0) {
            scrollUp(1);
        }
    }


    public void nextLine() {

        locateToCaretScope();

        if (caretPosOnContent() + caretRemainingOnRow() >= content.length() - 1) {
            end();
            next();
            return;
        }

        int remaining = rows.get(caretRowOnScreen).length() - caretOffsetOnRow();
        caretRowOnScreen++;
        setCaretOffset(getCaretOffset() + remaining + caretOffsetOnRow());

        if (caretRowOnScreen + 2 > rowSize && bottomLengthOnContent() <= content.length()) {
            scrollDown(1);
        }
    }


    public void prevLine() {

        locateToCaretScope();

        if (headRowOnContent == 0 && caretRowOnScreen == 0) {
            return;
        }

        int remaining = caretOffsetOnRow();
        caretRowOnScreen--;
        setCaretOffset(getCaretOffset() - remaining - (rows.get(caretRowOnScreen).length() - caretOffsetOnRow()));

        if (headRowOnContent > 0 && caretRowOnScreen - 1 < 0) {
            scrollUp(1);
        }
    }


    public void home() {
        locateToCaretScope();
        int remaining = caretOffsetOnRow();
        caretLogicalOffsetOnRow = 0;
        setCaretOffset(getCaretOffset() - remaining);
    }


    public void end() {
        locateToCaretScope();
        int remaining = caretRowTextLength() - caretOffsetOnRow();
        caretLogicalOffsetOnRow = caretRowTextLength();
        setCaretOffset(getCaretOffset() + remaining);
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
            setCaretOffset(getCaretOffset() + firstRow.length());
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
            setCaretOffset(getCaretOffset() - removeLineLength);
        }
    }

    public void pageUp() {
        for (int i = 0; i < rowSize; i++) {
            prevLine();
        }
    }

    public void pageDown() {
        for (int i = 0; i < rowSize; i++) {
            nextLine();
        }
    }

    public void delete() {
        if (rows.get(caretRowOnScreen).charAt(caretOffsetOnRow()) == '\n') {
            content.delete(caretPosOnContent(), 1);
            fillRows(rowSize + 1);
            String edited = rows.get(caretRowOnScreen);
            String nextLine = "";
            if ((rows.size() > caretRowOnScreen + 1)) {
                nextLine = rows.get(caretRowOnScreen + 1);
                rows.remove(caretRowOnScreen + 1);
            }
            rows.set(caretRowOnScreen, edited.substring(0, caretOffsetOnRow()) + nextLine);
        } else {
            content.delete(caretPosOnContent(), 1);
            String edited = rows.get(caretRowOnScreen);
            rows.set(caretRowOnScreen,
                edited.substring(0, caretOffsetOnRow()) +
                    edited.substring(caretOffsetOnRow() + 1));
        }
    }


    public void backSpace() {
        int pos = caretPosOnContent();
        prev();
        if (pos != caretPosOnContent()) {
            delete();
        }
    }

    public void add(String string) {

        locateToCaretScope();

        content.insert(caretPosOnContent(), string);

        int caretOffsetOnRow = caretOffsetOnRow();
        String current = rows.get(caretRowOnScreen);
        String prefix = current.substring(0, caretOffsetOnRow);
        String suffix = current.substring(caretOffsetOnRow);
        List<String> lines = Strings.splitLine(prefix + string + suffix);
        rows.set(caretRowOnScreen, lines.get(0));
        caretLogicalOffsetOnRow = caretOffsetOnRow + lines.get(0).length();
        for (int i = 1; i < lines.size(); i++) {
            rows.add(caretRowOnScreen + i, lines.get(i));
            caretLogicalOffsetOnRow = lines.get(i).length() - suffix.length();
        }
        caretRowOnScreen += lines.size() - 1;
        setCaretOffset(getCaretOffset() + string.length());

        fillRows(rowSize);
        locateToCaretScope();
    }


    void locateToCaretScope() {
        if (caretOffset.get() < 0) {
            while (caretOffset.get() < 0) {
                scrollUp(1);
            }
            scrollUp(1);
        }
        if (caretRowOnScreen > rowSize) {
            while (caretRowOnScreen > rowSize) {
                scrollDown(1);
            }
            scrollDown(3);
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
     *         5 textLengthOnScreen
     * </pre>
     */
    int textLengthOnScreen() {
        return rows.stream().mapToInt(String::length).sum();
    }



    void setRowSize(int preferenceSize) {
        fillRows(preferenceSize);
        rowSize = preferenceSize;
    }



    private void fillRows(int preferenceSize) {

        if (preferenceSize == rows.size()) {
            // just
            return;
        }

        while (preferenceSize < rows.size()) {
            // if the size of the rows is too large, reduce it from the end.
            rows.remove(rows.size() - 1);
        }

        if (preferenceSize > rows.size()) {
            // if the size of the rows is small, add a line at the end.
            for (int i = bottomLengthOnContent(); i < content.length() && preferenceSize > rows.size();) {
                String string = content.untilEol(i);
                i += (string.length() == 0) ? 1 : string.length();
                rows.add(string);
            }
        }
    }


    private String getScreenString(int beginIndexOnScreen, int endIndexOnscreen) {
        return String.join("", rows).substring(beginIndexOnScreen, endIndexOnscreen);
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


    public final int getCaretOffset() { return caretOffset.get(); }
    public final void setCaretOffset(int value) { caretOffset.set(value); }
    public IntegerProperty caretOffsetProperty() { return caretOffset; }

}
