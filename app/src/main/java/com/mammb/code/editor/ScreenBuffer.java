package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import static java.lang.System.Logger.Level.*;

public class ScreenBuffer {

    private static final System.Logger logger = System.getLogger(ScreenBuffer.class.getName());

    final ObservableList<String> rows = FXCollections.observableArrayList("");

    /** Caret row on the text flow. */
    private int caretOffsetY = 0;
    /** Caret offset on the row. May be larger than the number of characters in a row. */
    private int caretOffsetX = 0;
    /** Offset on the text flow. */
    private IntegerProperty caretOffset = new SimpleIntegerProperty();
    /** screenRowSize. */
    private int screenRowSize = 1;

    private final Content content = new EditBufferedContent(new PtContent());
    private int originRowIndex = 0;
    private int originIndex = 0;


    public ScreenBuffer() {
    }


    public void open(Path path) {
        content.open(path);
        originRowIndex = originIndex = caretOffsetY = caretOffsetX = 0;
        caretOffset.set(0);
        rows.clear();
        setScreenRowSize(screenRowSize);
    }


    public void next() {

        scrollToCaret();

        if (caretIndex() >= content.length()) {
            return;
        }

        caretOffsetX = caretLineCharOffset(); // reset logical position
        caretOffsetX++;
        if (caretOffsetX > caretLineCharLength()) {
            // move caret to the next line head
            caretOffsetX = 0;
            caretOffsetY++;
        }
        setCaretOffset(getCaretOffset() + 1);

        if (caretOffsetY + 2 > screenRowSize && lastIndexOnScreen() <= content.length()) {
            scrollNext(1);
        }

    }


    public void prev() {

        scrollToCaret();

        if (originRowIndex == 0 && caretOffsetY == 0 && getCaretOffset() == 0) {
            return;
        }
        caretOffsetX = caretLineCharOffset(); // reset logical position

        if (caretOffsetY > 0 && caretOffsetX == 0) {
            // move caret to the prev line tail
            caretOffsetY--;
            caretOffsetX = caretLineCharLength();
        } else {
            caretOffsetX--;
        }
        setCaretOffset(getCaretOffset() - 1);

        if (originRowIndex > 0 && caretOffsetY - 1 < 0) {
            scrollPrev(1);
        }
    }


    public void nextLine() {

        scrollToCaret();

        if (caretIndex() + caretLineCharRemaining() >= content.length() - 1) {
            end();
            next();
            return;
        }

        int remaining = rows.get(caretOffsetY).length() - caretLineCharOffset();
        caretOffsetY++;
        setCaretOffset(getCaretOffset() + remaining + caretLineCharOffset());

        if (caretOffsetY + 2 > screenRowSize && lastIndexOnScreen() <= content.length()) {
            scrollNext(1);
        }
    }


    public void prevLine() {

        scrollToCaret();

        if (originRowIndex == 0 && caretOffsetY == 0) {
            return;
        }

        int remaining = caretLineCharOffset();
        caretOffsetY--;
        setCaretOffset(getCaretOffset() - remaining - (rows.get(caretOffsetY).length() - caretLineCharOffset()));
        if (originRowIndex > 0 && caretOffsetY - 1 < 0) {
            scrollPrev(1);
        }
    }


    public void home() {
        scrollToCaret();
        int remaining = caretLineCharOffset();
        caretOffsetX = 0;
        setCaretOffset(getCaretOffset() - remaining);
    }


    public void end() {
        scrollToCaret();
        int remaining = caretLineCharLength() - caretLineCharOffset();
        caretOffsetX = caretLineCharLength();
        setCaretOffset(getCaretOffset() + remaining);
    }


    public void scrollPrev(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (originRowIndex == 0) return;

            String firstRow = content.untilSol(originIndex - 1);
            rows.add(0, firstRow);
            if (rows.size() > screenRowSize) {
                rows.remove(rows.size() - 1);
            }
            originRowIndex--;
            originIndex -= firstRow.length();

            caretOffsetY++;
            setCaretOffset(getCaretOffset() + firstRow.length());
        }
    }


    public void scrollNext(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (rows.size() < (int) Math.ceil(screenRowSize * 2.0 / 3.0)) return;

            int removeLineLength = rows.get(0).length();
            int bottomLengthOnContent = lastIndexOnScreen();
            if (bottomLengthOnContent < content.length()) {
                rows.add(content.untilEol(bottomLengthOnContent));
            }
            rows.remove(0);
            originRowIndex++;
            originIndex += removeLineLength;

            caretOffsetY--;
            setCaretOffset(getCaretOffset() - removeLineLength);
        }
    }

    public void pageUp() {
        for (int i = 0; i < screenRowSize; i++) {
            prevLine();
        }
    }

    public void pageDown() {
        for (int i = 0; i < screenRowSize; i++) {
            nextLine();
        }
    }

    public void delete(int len) {

        int caretIndex = caretIndex();
        if (caretIndex + len > content.length()) return;

        scrollToCaret();

        String deleteString = peekString(caretIndex, caretIndex + len);
        String[] deleteLines = Strings.splitLine(deleteString);

        content.delete(caretIndex, len);

        if (deleteLines.length == 1) {
            // simple inline delete
            String line = rows.get(caretOffsetY);
            int index = caretLineCharOffset();
            rows.set(caretOffsetY,
                line.substring(0, index) + line.substring(index + 1));
        } else {
            int deleteLineCount = deleteLines.length - 1;
            fillRows(screenRowSize + deleteLineCount);
            String prefix = rows.get(caretOffsetY).substring(0, caretLineCharOffset());
            String suffix = "";
            if (caretOffsetY + deleteLineCount < rows.size()) {
                suffix = rows.get(caretOffsetY + deleteLineCount).substring(deleteLines[deleteLineCount].length());
            }
            rows.set(caretOffsetY, prefix + suffix);
            for (int i = 0; i < deleteLineCount; i++) {
                rows.remove(caretOffsetY + 1);
            }
        }
    }


    public void backSpace() {
        int pos = caretIndex();
        prev();
        if (pos != caretIndex()) {
            delete(1);
        }
    }

    public void add(String string) {

        scrollToCaret();

        content.insert(caretIndex(), string);

        int caretLineCharOffset = caretLineCharOffset();
        String current = rows.get(caretOffsetY);
        String prefix = current.substring(0, caretLineCharOffset);
        String suffix = current.substring(caretLineCharOffset);
        String[] lines = Strings.splitLine(prefix + string + suffix);
        rows.set(caretOffsetY, lines[0]);
        caretOffsetX = caretLineCharOffset + string.length();
        for (int i = 1; i < lines.length; i++) {
            rows.add(caretOffsetY + i, lines[i]);
            caretOffsetX = lines[i].length() - suffix.length();
        }
        caretOffsetY += lines.length - 1;
        setCaretOffset(getCaretOffset() + string.length());

        fillRows(screenRowSize);
        scrollToCaret();
    }


    void scrollToCaret() {

        if (isCaretVisibleOnScreen()) {
            return;
        }
        if (caretOffset.get() < 0) {
            while (caretOffset.get() < 0) {
                scrollPrev(1);
            }
            scrollPrev(1);
        }
        if (caretOffsetY > screenRowSize) {
            while (caretOffsetY > screenRowSize) {
                scrollNext(1);
            }
            scrollNext(3);
        }
    }


    /**
     * <pre>
     *    |x|x|↵  0
     *    |x|x|↵  1
     * ---------
     * 0: |a|b|↵  2  ← originRowIndex 2
     * 1: |c|d|                         ↓
     * ↑ caretOffsetY 1  → caretRowIndex 3
     * </pre>
     */
    public int caretRowIndex() {
        return originRowIndex + caretOffsetY;
    }


    /**
     * <pre>
     * |x|x|↵
     * ---------
     *  ↓ originIndex 3
     * |a|b|↵              ↓
     * |c|d|               ↓
     *   ↑ caretOffset 4 → caretIndex 7
     * </pre>
     */
    public int caretIndex() {
        return originIndex + caretOffset.get();
    }


    /**
     * <pre>
     * |x|x|↵
     * ---------
     *  ↓ originIndex 3
     * |a|b|↵  3            ↓
     * |c|d|   2           lastIndexOnScreen 8
     *       -----          ↑
     *         5 lengthOnScreen
     * </pre>
     */
    public int lastIndexOnScreen() {
        return originIndex + charCountOnScreen();
    }


    /**
     * <pre>
     * |c|o|n|t|e|n|t|↵
     *       ↑ caretLogicalOffsetOnRow
     *       ↑ caretLineCharOffset
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|↵
     *               ↑      ↑ caretOffsetX
     *               └ caretLineCharOffset
     * </pre>
     */
    public int caretLineCharOffset() {
        return Math.min(caretLineCharLength(), caretOffsetX);
    }

    /**
     * <pre>
     * |c|o|n|t|e|n|t|↵
     *         ↑     ↑ caretLineCharLength
     *         └ caretOffsetOnRow
     *         │← 3 →│ caretLineCharRemaining
     * </pre>
     */
    public int caretLineCharRemaining() {
        return caretLineCharLength() - caretLineCharOffset();
    }


    /**
     * <pre>
     * |c|o|n|t|e|n|t|↵
     * │←     7     →│ caretLineCharLength
     *                 ignore LF
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|
     * │←     7     →│ caretLineCharLength
     * </pre>
     */
    public int caretLineCharLength() {
        if (caretOffsetY >= rows.size() || caretOffsetY < 0) {
            return 0;
        }
        String row = rows.get(caretOffsetY);
        return row.endsWith("\n") ? row.length() - 1 : row.length();
    }


    /**
     * <pre>
     * |a|b|↵  3
     * |c|d|   2
     *       -----
     *         5 charCountOnScreen
     * </pre>
     */
    int charCountOnScreen() {
        return rows.stream().mapToInt(String::length).sum();
    }



    void setScreenRowSize(int preferenceSize) {
        fillRows(preferenceSize);
        screenRowSize = preferenceSize;
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
            for (int i = lastIndexOnScreen(); i < content.length() && preferenceSize > rows.size();) {
                String string = content.untilEol(i);
                if (string == null || string.isEmpty()) {
                    return;
                }
                i += string.length();
                rows.add(string);
            }
        }
    }


    private int getCaretCodePoint() {
        int caretPosOnContent = caretIndex();
        if (caretPosOnContent >= content.length()) {
            return 0;
        }
        return isCaretVisibleOnScreen()
            ? rows.get(caretOffsetY).codePointAt(caretLineCharOffset())
            : content.codePointAt(caretPosOnContent);
    }

    private boolean isCaretVisibleOnScreen() {
        return caretOffset.get() >= 0 && caretOffsetY <= screenRowSize;
    }


    String peekString(int beginIndex, int endIndex) {
        return content.substring(beginIndex, endIndex);
    }


    public final void moveCaret(int offset) {
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            int len = rows.get(i).length();
            index += len;
            if (index > offset) {
                caretOffsetY = i;
                caretOffsetX = len - (index - offset);
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

    public int getCaretOffsetY() {
        return caretOffsetY;
    }

    public int getCaretOffsetX() {
        return caretOffsetX;
    }

    public int getScreenRowSize() {
        return screenRowSize;
    }

    public int getOriginRowIndex() {
        return originRowIndex;
    }

    public int getOriginIndex() {
        return originIndex;
    }

    Content getContent() {
        return content;
    }

    public void inspect() {
        logger.log(INFO, "========================================");
        logger.log(INFO, "rows.size[{0}], rowSize[{1}]", rows.size(), screenRowSize);
        logger.log(INFO, "headRowOnContent[{0}], headPosOnContent[{1}]", originRowIndex, originIndex);
        logger.log(INFO, "caretOffsetY[{0}], caretOffsetX[{1}], caretOffset[{2}]", caretOffsetY, caretOffsetX, caretOffset.get());
        if (content instanceof EditBufferedContent editBuffered) {
            logger.log(INFO, "editBuffered.getEdit() : {0}", editBuffered.getEdit());
        }
    }

    public void reset() {
        logger.log(INFO, "== reset");
        originRowIndex = originIndex = caretOffsetY = caretOffsetX = 0;
        caretOffset.set(0);
    }
}
