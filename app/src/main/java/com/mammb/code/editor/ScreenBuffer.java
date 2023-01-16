package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mammb.code.piecetable.Edited;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
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
    private final IntegerProperty caretOffset = new SimpleIntegerProperty();
    /** screenRowSize. */
    private final IntegerProperty screenRowSize = new SimpleIntegerProperty(1);

    private final Content content = new EditBufferedContent(new ContentImpl());
    private final IntegerProperty originRowIndex = new SimpleIntegerProperty();
    private final IntegerProperty originIndex    = new SimpleIntegerProperty();

    private final ReadOnlyIntegerWrapper totalLines = new ReadOnlyIntegerWrapper(1);
    private final ReadOnlyIntegerWrapper scrollMaxLines = new ReadOnlyIntegerWrapper(1);

    private final List<Function<Integer, Integer>> dirtyListener = new ArrayList<>();


    public ScreenBuffer() {
        totalLines.addListener(this::updateScrollMaxLines);
        screenRowSize.addListener(this::updateScrollMaxLines);
    }


    public void open(Path path) {
        content.open(path);
        totalLines.set(content.lineCount());

        caretOffsetY = caretOffsetX = 0;
        caretOffset.set(0);
        originRowIndex.set(0);
        originIndex.set(0);
        dirtyListener.forEach(l -> l.apply(0));
        rows.clear();
        fitRows(getScreenRowSize());

        originRowIndex.set(-1);
        originRowIndex.set(0);

    }

    public void addDirtyListener(Function<Integer, Integer> listener) {
        dirtyListener.add(listener);
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

        // reset logical position
        caretOffsetX = caretLineCharOffset();

        int n = getNextIndexSize();
        caretOffsetX += n;

        if (caretOffsetX > caretLineCharLength()) {
            // move caret to the next line head
            caretOffsetX = 0;
            caretOffsetY++;
        }
        setCaretOffset(getCaretOffset() + n);

        if (caretOffsetY + 2 > getScreenRowSize() && lastIndexOnScreen() <= content.length()) {
            // scroll if the caret comes to the bottom of the screen
            scrollNext(1);
        }

    }


    public void prev() {

        scrollToCaret();

        if (getOriginRowIndex() == 0 && caretOffsetY == 0 && getCaretOffset() == 0) {
            return;
        }

        // reset logical position
        caretOffsetX = caretLineCharOffset();

        int n = getPrevIndexSize();
        if (caretOffsetY > 0 && caretOffsetX == 0) {
            // move caret to the prev line tail
            caretOffsetY--;
            caretOffsetX = caretLineCharLength();
        } else {
            caretOffsetX -= n;
        }
        setCaretOffset(getCaretOffset() - n);

        if (getOriginRowIndex() > 0 && caretOffsetY - 1 < 0) {
            // scroll if the caret comes to the head of the screen.
            scrollPrev(1);
        }
    }


    public void nextLine() {

        scrollToCaret();

        if (caretIndex() >= content.length() ||
                !rows.get(caretOffsetY).endsWith("\n")) {
            return;
        }

        int remaining = caretLineRemaining();
        caretOffsetY++;

        //     +-------+ remaining
        //  x x|x x x $ ->  x x x x x $
        //  x x x x         x x|x x
        //                  +--+ caretLineCharOffset
        setCaretOffset(getCaretOffset() + remaining + caretLineCharOffset());

        if (caretOffsetY + 2 > getScreenRowSize() && lastIndexOnScreen() <= content.length()) {
            // scroll if the caret comes to the bottom of the screen
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

        //                 +---+ caretLineCharOffset
        //                 +-----------+ rows.get(caretOffsetY).length()
        //  x x x x x $ ->  x x|x x x $
        //  x x|x x         x x x x
        // +---+ remaining
        setCaretOffset(getCaretOffset() - remaining - rows.get(caretOffsetY).length() + caretLineCharOffset());

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

            if (rows.size() < prefRowRemaining()) return;

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

    public void pageScrollUp() {
        scrollPrev(getScreenRowSize());
    }

    public void pageDown() {
        for (int i = 0; i < getScreenRowSize(); i++) {
            nextLine();
        }
    }

    public void pageScrollDown() {
        scrollNext(getScreenRowSize());
    }


    public void scrollTo(int rowIndex) {
        if (rowIndex < getOriginRowIndex()) {
            scrollPrev(getOriginRowIndex() - rowIndex);
        } else if (rowIndex > getOriginRowIndex()) {
            scrollNext(rowIndex - getOriginRowIndex());
        }
    }


    public void delete(int len) {

        if (len <= 0) return;

        int caretIndex = caretIndex();
        if (caretIndex + len > content.length()) return;

        scrollToCaret();

        String deleteString = peekString(caretIndex, caretIndex + len);
        String[] deleteLines = Strings.splitLine(deleteString);

        int deleteLineCount = deleteLines.length - 1;
        if (deleteLineCount > 0) {
            // Pre-supply the number of rows to be deleted
            fitRows(getScreenRowSize() + deleteLineCount);
        }
        content.delete(caretIndex, len);
        totalLines.set(getTotalLines() - deleteLineCount);

        if (deleteLineCount == 0) {
            // simple inline delete
            //  a|b c d $  ->  del:[b c]  ->  a|d $
            String line = rows.get(caretOffsetY);
            int index = caretLineCharOffset();
            dirtyListener.forEach(l -> l.apply(caretRowIndex()));
            rows.set(caretOffsetY,
                line.substring(0, index) + line.substring(index + deleteString.length()));

        } else {
            // multi rows delete
            // +-------+ prefix
            //  x x x x|- -          ->    x x x x|x x x
            //  - - - - - -
            //  - - x x x
            //     +-----+ suffix
            String prefix = rows.get(caretOffsetY).substring(0, caretLineCharOffset());
            String suffix = "";
            if (caretOffsetY + deleteLineCount < rows.size()) {
                suffix = rows.get(caretOffsetY + deleteLineCount).substring(deleteLines[deleteLineCount].length());
            }
            dirtyListener.forEach(l -> l.apply(caretRowIndex()));
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

        if (string == null) return;

        scrollToCaret();

        boolean caretTailed = caretIndex() == content.length();
        content.insert(caretIndex(), string);
        totalLines.set(getTotalLines() + Strings.countLf(string));

        int caretLineCharOffset = caretLineCharOffset();
        String current = rows.get(caretOffsetY);
        String prefix = current.substring(0, caretLineCharOffset);
        String suffix = current.substring(caretLineCharOffset);

        if (Strings.countLf(string) == 0) {
            // simple inline add
            String line = prefix + string + suffix;
            dirtyListener.forEach(l -> l.apply(caretRowIndex()));
            rows.set(caretOffsetY, line);
            caretOffsetX = caretLineCharOffset + string.length();
            setCaretOffset(getCaretOffset() + string.length());
        } else {
            // multi rows add
            String[] lines = caretTailed
                ? Strings.splitLine(prefix + string + suffix)
                : Strings.splitLf(prefix + string + suffix);
            dirtyListener.forEach(l -> l.apply(caretRowIndex()));
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


    void undo() { redraw(content.undo()); }


    void redo() { redraw(content.redo()); }


    private void redraw(Edited edited) {
        if (!edited.isEmpty())  {
            dirtyListener.forEach(l -> l.apply(edited.pos()));
            redraw(edited.pos(), edited.pos() + edited.len());
        }
    }


    private void redraw(int startIndex, int endIndex) {
        while (startIndex < originIndex.get()) {
            scrollPrev(3);
        }
        while (startIndex > originIndex.get() + charCountOnScreen()) {
            scrollNext(3);
        }

        moveCaret(startIndex - originIndex.get());

        while (rows.size() > caretOffsetY) {
            rows.remove(rows.size() - 1);
        }

        fitRows(screenRowSize.get());
        totalLines.set(content.lineCount());
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
     *         |<-3->| caretLineCharRemaining
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
     *         |<- 4 ->| caretLineRemaining
     * </pre>
     */
    public int caretLineRemaining() {
        return rows.get(caretOffsetY).length() - caretLineCharOffset();
    }

    /**
     * <pre>
     * |c|o|n|t|e|n|t|$
     * |<-    7    ->| caretLineCharLength
     *                 ignore LF
     * </pre>
     * <pre>
     * |c|o|n|t|e|n|t|
     * |<-    7    ->| caretLineCharLength
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


    private int toOffsetY(int offset) {
        for (int i = 0, count = 0; i < rows.size(); i++) {
            count += rows.get(i).length();
            if (count > offset) {
                return i;
            }
        }
        return rows.size();
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
                    break;
                }
                i += string.length();
                rows.add(string);
            }
        }

        if (rows.isEmpty()) {
            rows.add("");
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


    private int getCaretPrevCodePoint() {
        int caretPosOnContent = caretIndex() - 1;
        if (caretPosOnContent <= 0) {
            return 0;
        }
        if (isCaretVisibleOnScreen() && caretOffset.get() > 1) {
            if (caretOffsetX == 0) {
                return '\n';
            } else {
                char ch = rows.get(caretOffsetY).charAt(caretLineCharOffset() - 1);
                return Character.isLowSurrogate(ch)
                    ? rows.get(caretOffsetY).codePointAt(caretLineCharOffset() - 2)
                    : rows.get(caretOffsetY).codePointAt(caretLineCharOffset() - 1);
            }
        } else {
            return content.codePointAt(caretPosOnContent);
        }
    }

    private int getNextIndexSize() {
        return Character.charCount(getCaretCodePoint());
    }

    private int getPrevIndexSize() {
        return Character.charCount(getCaretPrevCodePoint());
    }


    private boolean isCaretVisibleOnScreen() {
        return caretOffset.get() >= 0 && caretOffsetY <= getScreenRowSize();
    }


    public String caretLineText() {
        scrollToCaret();
        return rows.get(caretOffsetY);
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


    public int[] wordRange(int offset) {

        int[] ret = new int[] { offset, offset };

        int[] left  = content.untilSol(getOriginIndex() + offset).toLowerCase().codePoints().toArray();
        int[] right = content.untilEol(getOriginIndex() + offset).toLowerCase().codePoints().toArray();

        for (int i = left.length - 1; i >= 0; i--) {
            if (Character.getType(left[left.length - 1]) != Character.getType(left[i])) {
                ret[0] -= (left.length - i - 2);
                break;
            }
        }
        for (int i = 0; i < right.length; i++) {
            if (Character.getType(right[0]) != Character.getType(right[i])) {
                ret[1] += i;
                break;
            }
        }
        return ret;
    }


    private void updateScrollMaxLines(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        scrollMaxLines.set(totalLines.get() - prefRowRemaining());
    }

    /**
     * Number of lines to remain when scrolling.
     * @return the number of lines to remain when scrolling
     */
    private int prefRowRemaining() {
        return (int) Math.ceil(getScreenRowSize() * 2.0 / 3.0);
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

    public boolean isDirty() {
        return content.undoSize() > 0;
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
        totalLines.set(1);
        fitRows(getScreenRowSize());
    }

    // <editor-fold desc="properties">

    public final int getOriginRowIndex() { return originRowIndex.get(); }
    void setOriginRowIndex(int value) { originRowIndex.set(value); }
    public IntegerProperty originRowIndexProperty() { return originRowIndex; }

    public final int getOriginIndex() { return originIndex.get(); }
    void setOriginIndex(int value) { originIndex.set(value); }
    public IntegerProperty originIndexProperty() { return originIndex; }

    public final int getScreenRowSize() { return screenRowSize.get(); }
    void setScreenRowSize(int value) { screenRowSize.set(value); }
    public IntegerProperty screenRowSizeProperty() { return screenRowSize; }

    public final int getTotalLines() { return totalLines.get(); }
    public ReadOnlyIntegerProperty totalLinesProperty() { return totalLines; }

    public final int getScrollMaxLines() { return scrollMaxLines.get(); }
    public ReadOnlyIntegerProperty scrollMaxLinesProperty() { return scrollMaxLines; }

    // </editor-fold>

}

