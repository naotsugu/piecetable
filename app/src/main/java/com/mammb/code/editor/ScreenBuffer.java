package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.Arrays;
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
    private IntegerProperty screenRowSize = new SimpleIntegerProperty();

    private final Content content = new EditBufferedContent(new PtContent());
    private IntegerProperty originRowIndex = new SimpleIntegerProperty();
    private IntegerProperty originIndex    = new SimpleIntegerProperty();

    public ScreenBuffer() {
    }


    public void open(Path path) {
        content.open(path);
        caretOffsetY = caretOffsetX = 0;
        screenRowSize.set(1);
        caretOffset.set(0);
        originRowIndex.set(0);
        originIndex.set(0);
        rows.clear();
        setupScreenRowSize(getScreenRowSize());
    }

    public void save() {
        content.write();
        logger.log(INFO, "== saved :" + content.path());
    }

    public void saveAs(Path path) {
        content.write(path);
        logger.log(INFO, "== saved :" + content.path());
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

        if (caretOffsetY + 2 > getScreenRowSize() && lastIndexOnScreen() <= content.length()) {
            scrollNext(1);
        }

    }


    public void prev() {

        scrollToCaret();

        if (getOriginRowIndex() == 0 && caretOffsetY == 0 && getCaretOffset() == 0) {
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

        if (getOriginRowIndex() > 0 && caretOffsetY - 1 < 0) {
            scrollPrev(1);
        }
    }


    public void nextLine() {

        scrollToCaret();

        if (caretIndex() >= content.length() || !rows.get(caretOffsetY).endsWith("\n")) {
            return;
        }

        int remaining = caretLineRemaining();
        caretOffsetY++;
        setCaretOffset(getCaretOffset() + remaining + caretLineCharOffset());

        if (caretOffsetY + 2 > getScreenRowSize() && lastIndexOnScreen() <= content.length()) {
            scrollNext(1);
        }
    }


    public void prevLine() {

        scrollToCaret();

        if (getOriginRowIndex() == 0 && caretOffsetY == 0) {
            return;
        }

        int remaining = caretLineCharOffset();
        caretOffsetY--;
        setCaretOffset(getCaretOffset() - remaining - (rows.get(caretOffsetY).length() - caretLineCharOffset()));
        if (getOriginRowIndex() > 0 && caretOffsetY - 1 < 0) {
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

            if (getOriginRowIndex() == 0) return;

            String firstRow = content.untilSol(getOriginIndex() - 1);
            rows.add(0, firstRow);
            if (rows.size() > getScreenRowSize()) {
                rows.remove(rows.size() - 1);
            }
            setOriginRowIndex(getOriginRowIndex() - 1);
            setOriginIndex(getOriginIndex() - firstRow.length());

            caretOffsetY++;
            setCaretOffset(getCaretOffset() + firstRow.length());
        }
    }


    public void scrollNext(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (rows.size() < (int) Math.ceil(getScreenRowSize() * 2.0 / 3.0)) return;

            int removeLineLength = rows.get(0).length();
            int bottomLengthOnContent = lastIndexOnScreen();
            if (bottomLengthOnContent < content.length()) {
                rows.add(content.untilEol(bottomLengthOnContent));
            }
            rows.remove(0);
            setOriginRowIndex(getOriginRowIndex() + 1);
            setOriginIndex(getOriginIndex() + removeLineLength);

            caretOffsetY--;
            setCaretOffset(getCaretOffset() - removeLineLength);
        }
    }

    public void pageUp() {
        for (int i = 0; i < getScreenRowSize(); i++) {
            prevLine();
        }
    }

    public void pageDown() {
        for (int i = 0; i < getScreenRowSize(); i++) {
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
            fitRows(getScreenRowSize() + deleteLineCount);
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

        boolean caretTailed = caretIndex() == content.length();
        content.insert(caretIndex(), string);

        int caretLineCharOffset = caretLineCharOffset();
        String current = rows.get(caretOffsetY);
        String prefix = current.substring(0, caretLineCharOffset);
        String suffix = current.substring(caretLineCharOffset);

        if (Strings.countLf(string) == 0) {
            // simple inline add
            String line = prefix + string + suffix;
            rows.set(caretOffsetY, line);
            caretOffsetX = caretLineCharOffset + string.length();
            setCaretOffset(getCaretOffset() + string.length());
        } else {
            String[] lines = caretTailed
                ? Strings.splitLine(prefix + string + suffix)
                : Strings.splitLf(prefix + string + suffix);
            rows.set(caretOffsetY, lines[0]);
            for (int i = 1; i < lines.length; i++) {
                rows.add(caretOffsetY + i, lines[i]);
            }
            caretOffsetX = lines[lines.length - 1].length() - suffix.length();
            caretOffsetY += lines.length - 1;
            setCaretOffset(getCaretOffset() + string.length());

            if (content instanceof EditBufferedContent buffer) buffer.flush();
            fitRows(getScreenRowSize());
            scrollToCaret();
        }

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
        if (caretOffsetY > getScreenRowSize()) {
            while (caretOffsetY > getScreenRowSize()) {
                scrollNext(1);
            }
            scrollNext(3);
        }
    }

    void undo() {
        int[] range = content.undo();
        logger.log(INFO, "undo" + Arrays.toString(range));
    }

    void redo() {
        int[] range = content.redo();
        logger.log(INFO, "redo" + Arrays.toString(range));
    }


    /**
     * <pre>
     *    |x|x|$  0
     *    |x|x|$  1
     * ---------
     * 0: |a|b|$  2  <- originRowIndex 2
     * 1: |c|d|                        |
     * L caretOffsetY 1  -> caretRowIndex 3
     * </pre>
     */
    public int caretRowIndex() {
        return getOriginRowIndex() + caretOffsetY;
    }


    /**
     * <pre>
     * |x|x|$
     * ---------
     *  + originIndex  3
     * |a|b|$          |
     * |c|d|           |
     *   L caretOffset 4 -> caretIndex 7
     * </pre>
     */
    public int caretIndex() {
        return getOriginIndex() + caretOffset.get();
    }


    /**
     * <pre>
     * |x|x|$
     * ---------
     *  + originIndex 3
     * |a|b|$  3      |
     * |c|d|   2      | lastIndexOnScreen 8
     *       -----    |
     *         5 lengthOnScreen
     * </pre>
     */
    public int lastIndexOnScreen() {
        return getOriginIndex() + charCountOnScreen();
    }


    /**
     * <pre>
     * |c|o|n|t|e|n|t|$
     *       L caretOffsetX
     *       L caretLineCharOffset
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|$
     *               |      L caretOffsetX
     *               └ caretLineCharOffset
     * </pre>
     */
    public int caretLineCharOffset() {
        return Math.min(caretLineCharLength(), caretOffsetX);
    }

    /**
     * <pre>
     * |c|o|n|t|e|n|t|$
     *         |     L caretLineCharLength
     *         └ caretLineCharOffset
     *         │<-3->│ caretLineCharRemaining
     * </pre>
     */
    public int caretLineCharRemaining() {
        return caretLineCharLength() - caretLineCharOffset();
    }

    /**
     * <pre>
     * |c|o|n|t|e|n|t|$
     *         |       |
     *         └ caretLineCharOffset
     *         │<- 4 ->│ caretLineRemaining
     * </pre>
     */
    public int caretLineRemaining() {
        return rows.get(caretOffsetY).length() - caretLineCharOffset();
    }

    /**
     * <pre>
     * |c|o|n|t|e|n|t|$
     * │<-    7    ->│ caretLineCharLength
     *                 ignore LF
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|
     * │<-    7    ->│ caretLineCharLength
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
     * |a|b|$  3
     * |c|d|   2
     *       -----
     *         5 charCountOnScreen
     * </pre>
     */
    int charCountOnScreen() {
        return rows.stream().mapToInt(String::length).sum();
    }



    void setupScreenRowSize(int preferenceSize) {
        fitRows(preferenceSize);
        setScreenRowSize(preferenceSize);
    }



    private void fitRows(int preferenceSize) {

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
        return caretOffset.get() >= 0 && caretOffsetY <= getScreenRowSize();
    }


    String peekString(int beginIndex, int endIndex) {
        return content.substring(beginIndex, endIndex);
    }


    public final void moveCaret(int offset) {
        int charCountOnScreen = charCountOnScreen();
        if (offset > charCountOnScreen) {
            caretOffsetY = rows.size() - 1;
            caretOffsetX = rows.get(caretOffsetY).length();
            caretOffset.set(charCountOnScreen);
            return;
        }
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            int len = rows.get(i).length();
            index += len;
            if (index > offset) {
                caretOffsetY = i;
                caretOffsetX = len - (index - offset);
                caretOffset.set(offset);
                break;
            }
        }
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

    Content getContent() {
        return content;
    }

    public Path getPath() {
        return content.path();
    }

    public void inspect() {
        logger.log(INFO, "========================================");
        logger.log(INFO, "rows.size[{0}], rowSize[{1}]", rows.size(), getScreenRowSize());
        logger.log(INFO, "originRowIndex[{0}], originIndex[{1}]", getOriginRowIndex(), getOriginIndex());
        logger.log(INFO, "caretOffsetY[{0}], caretOffsetX[{1}], caretOffset[{2}]", caretOffsetY, caretOffsetX, caretOffset.get());
        if (content instanceof EditBufferedContent editBuffered) {
            logger.log(INFO, "editBuffered.getEdit() : {0}", editBuffered.getEdit());
        }
    }

    public String inspectText() {
        var sb = new StringBuilder('\n');
        for (int i = 0; i < rows.size(); i++) {
            sb.append(getOriginRowIndex() + i).append(": ").append(rows.get(i).replace('\n', '$')).append('\n');
        }
        return sb.toString();
    }

    public void reset() {
        logger.log(INFO, "== reset");
        if (content instanceof EditBufferedContent buffer) buffer.flush();
        caretOffsetY = caretOffsetX = 0;
        caretOffset.set(0);
        originRowIndex.set(0);
        originIndex.set(0);
        if (content instanceof EditBufferedContent buffer) buffer.flush();
        rows.clear();
        fitRows(getScreenRowSize());
    }


    public final int getOriginRowIndex() { return originRowIndex.get(); }
    private void setOriginRowIndex(int value) { originRowIndex.set(value); }
    public IntegerProperty originRowIndexProperty() { return originRowIndex; }

    public final int getOriginIndex() { return originIndex.get(); }
    private void setOriginIndex(int value) { originIndex.set(value); }
    public IntegerProperty originIndexProperty() { return originIndex; }

    public final int getScreenRowSize() { return screenRowSize.get(); }
    private void setScreenRowSize(int value) { screenRowSize.set(value); }
    public IntegerProperty screenRowSizeProperty() { return screenRowSize; }


}

