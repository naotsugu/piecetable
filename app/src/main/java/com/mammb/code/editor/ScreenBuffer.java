package com.mammb.code.editor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    private final Content content = new BufferedContent(new ContentImpl());
    private final IntegerProperty originRowIndex = new SimpleIntegerProperty();
    private final IntegerProperty originIndex    = new SimpleIntegerProperty();
    /** caret index on content. */
    private int caretIndex = 0;

    private final ReadOnlyIntegerWrapper totalLines = new ReadOnlyIntegerWrapper(1);
    private final ReadOnlyIntegerWrapper scrollMaxLines = new ReadOnlyIntegerWrapper(1);

    private final List<EditListener> editListeners = new ArrayList<>();


    public ScreenBuffer() {
        totalLines.addListener(this::updateScrollMaxLines);
        screenRowSize.addListener(this::updateScrollMaxLines);
    }


    public void open(Path path) {
        content.open(path);
        totalLines.set(content.lineCount());

        caretOffsetY = caretOffsetX = 0;
        caretOffset.set(0);
        caretIndex = 0;
        originRowIndex.set(0);
        originIndex.set(0);
        rows.clear();
        fitRows(getScreenRowSize());

        originRowIndex.set(-1);
        originRowIndex.set(0);

    }


    public void add(String string) {

        if (string == null) return;

        scrollToCaret();

        int lineCount = Strings.countLf(string);
        boolean caretTailed = caretIndex == content.length();
        content.insert(caretIndex, string);
        totalLines.set(totalLines.get() + lineCount);

        int caretLineCharOffset = caretLineCharOffset();
        String current = rows.get(caretOffsetY);
        String prefix = current.substring(0, caretLineCharOffset);
        String suffix = current.substring(caretLineCharOffset);

        if (lineCount == 0) {
            // simple inline add
            String line = prefix + string + suffix;
            editListeners.forEach(l -> l.preEdit(getOriginRowIndex() + caretOffsetY, caretOffsetY, 1));
            rows.set(caretOffsetY, line);
            editListeners.forEach(EditListener::postEdit);
            caretOffsetX = caretLineCharOffset + string.length();
            caretOffset.set(caretOffset.get() + string.length());
            caretIndex += Strings.codePointCount(string);
        } else {
            // multi rows add
            String[] lines = caretTailed
                ? Strings.splitLine(prefix + string + suffix)
                : Strings.splitLf(prefix + string + suffix);
            editListeners.forEach(l -> l.preEdit(getOriginRowIndex() + caretOffsetY, caretOffsetY, lines.length));
            rows.set(caretOffsetY, lines[0]);
            for (int i = 1; i < lines.length; i++) {
                rows.add(caretOffsetY + i, lines[i]);
            }
            editListeners.forEach(EditListener::postEdit);
            caretOffsetX = lines[lines.length - 1].length() - suffix.length();
            caretOffsetY += lines.length - 1;
            caretOffset.set(caretOffset.get() + string.length());
            caretIndex += Strings.codePointCount(string);

            if (content instanceof BufferedContent buffer) buffer.flush();
            fitRows(getScreenRowSize());
            scrollToCaret();
        }
    }


    public void delete(int len) {

        if (len <= 0) return;
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
        totalLines.set(totalLines.get() - deleteLineCount);

        if (deleteLineCount == 0) {
            // simple inline delete
            //  a|b c d $  ->  del:[b c]  ->  a|d $
            String line = rows.get(caretOffsetY);
            int index = caretLineCharOffset();
            editListeners.forEach(l -> l.preEdit(getOriginRowIndex() + caretOffsetY, caretOffsetY, 1));
            rows.set(caretOffsetY,
                line.substring(0, index) + line.substring(index + deleteString.length()));
            editListeners.forEach(EditListener::postEdit);
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
            editListeners.forEach(l -> l.preEdit(getOriginRowIndex() + caretOffsetY, caretOffsetY, deleteLineCount + 1));
            rows.set(caretOffsetY, prefix + suffix);
            for (int i = 0; i < deleteLineCount; i++) {
                rows.remove(caretOffsetY + 1);
            }
            editListeners.forEach(EditListener::postEdit);
        }

        prepareTailRow();
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

        if (caretIndex >= content.length()) {
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
        caretOffset.set(caretOffset.get() + n);
        caretIndex++;

        if (caretOffsetY + 2 > getScreenRowSize() && lastIndexOnScreen() <= content.length()) {
            // scroll if the caret comes to the bottom of the screen
            scrollNext(1);
        }

    }


    public void prev() {

        scrollToCaret();

        if (getOriginRowIndex() == 0 && caretOffsetY == 0 && caretOffset.get() == 0) {
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
        caretOffset.set(caretOffset.get() - n);
        caretIndex--;

        if (getOriginRowIndex() > 0 && caretOffsetY - 1 < 0) {
            // scroll if the caret comes to the head of the screen.
            scrollPrev(1);
        }
    }


    public void nextLine() {

        scrollToCaret();

        if (caretIndex > content.length() - 1 || caretOffsetY == rows.size() - 1) {
            return;
        }

        //          +-------+
        // curr  x x|x x x $ ->  x x x x x $
        // next  x x x x         x x|x x
        //      +---+
        String curr = rows.get(caretOffsetY);
        String next = rows.get(caretOffsetY + 1).replace("\n", "");
        String across = curr.substring(caretLineCharOffset())
            + next.substring(0, Math.min(caretOffsetX, next.length()));
        caretOffset.set(caretOffset.get() + across.length());
        caretIndex += Strings.codePointCount(across);

        caretOffsetY++;
        if (caretOffsetY + 2 > getScreenRowSize() && lastIndexOnScreen() <= content.length()) {
            // scroll if the caret comes to the bottom of the screen
            scrollNext(1);
        }
    }


    public void prevLine() {

        scrollToCaret();

        if (getOriginRowIndex() == 0 && caretOffsetY <= 0) {
            return;
        }

        //          +-------+
        // prev  x x x x x $ ->  x x|x x x $
        // curr  x x|x x         x x x x
        //      +---+
        String prev = rows.get(caretOffsetY - 1);
        String curr = rows.get(caretOffsetY);

        int prevCharLen = prev.length() - 1; // end with a new line because there is a next line
        String across = prev.substring(Math.min(caretOffsetX, prevCharLen))
            + curr.substring(0, caretLineCharOffset());
        caretOffset.set(caretOffset.get() - across.length());
        caretIndex -= Strings.codePointCount(across);

        caretOffsetY--;
        if (originRowIndex.get() > 0 && caretOffsetY - 1 < 0) {
            scrollPrev(1);
        }
    }


    public void home() {
        scrollToCaret();
        int remaining = caretLineCharOffset();
        caretOffsetX = 0;
        caretOffset.set(caretOffset.get() - remaining);
        caretIndex -= Strings.codePointCount(rows.get(caretOffsetY), 0, remaining);
    }


    public void end() {
        scrollToCaret();
        int offset = caretLineCharOffset();
        int remaining = caretLineCharLength() - offset;
        caretOffsetX = caretLineCharLength();
        caretOffset.set(caretOffset.get() + remaining);
        caretIndex += caretLineCodePointLength() - offset;
    }


    public void scrollPrev(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (originRowIndex.get() == 0) return;

            String firstRow = content.untilSol(originIndex.get() - 1);
            originRowIndex.set(originRowIndex.get() - 1);
            originIndex.set(originIndex.get() - Strings.codePointCount(firstRow));
            rows.add(0, firstRow);
            if (rows.size() > screenRowSize.get()) {
                rows.remove(rows.size() - 1);
            }

            caretOffsetY++;
            caretOffset.set(caretOffset.get() + firstRow.length());
        }
    }


    public void scrollNext(int delta) {

        for (int i = 0; i < delta; i ++) {

            if (rows.size() < prefRowRemaining()) return;

            String removeLine = rows.get(0);
            int bottomLengthOnContent = lastIndexOnScreen();
            if (bottomLengthOnContent < content.length()) {
                rows.add(content.untilEol(bottomLengthOnContent));
            }
            rows.remove(0);
            originRowIndex.set(getOriginRowIndex() + 1);
            originIndex.set(originIndex.get() + Strings.codePointCount(removeLine));

            caretOffsetY--;
            caretOffset.set(caretOffset.get() - removeLine.length());
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


    public void backSpace() {
        int pos = caretIndex;
        prev();
        if (pos != caretIndex) {
            delete(1);
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


    void undo() { handleUndoOrRedo(true); }
    void redo() { handleUndoOrRedo(false); }

    void handleUndoOrRedo(boolean undo) {

        Edited future = undo ? content.undoFuture() : content.redoFuture();
        if (future.isEmpty()) return;

        int startIndex = future.pos();
        int lineCount = future.lineCount();
        while (startIndex < originIndex.get()) scrollPrev(3);
        while (startIndex > originIndex.get() + codePointCountOnScreen())  scrollNext(3);

        String ceiling = content.substring(originIndex.get(), startIndex);
        caretOffsetY = Strings.countLf(ceiling);
        caretOffsetX = Strings.lastLineLength(ceiling);
        caretOffset.set(ceiling.length());
        caretIndex = startIndex;

        editListeners.forEach(l -> l.preEdit(originRowIndex.get() + caretOffsetY, caretOffsetY, lineCount));

        if (undo) content.undo(); else content.redo();

        if (future.isInserted()) {
            totalLines.set(totalLines.get() + lineCount);
        } else {
            totalLines.set(totalLines.get() - lineCount);
        }

        // TODO reflect only the changes
        while (rows.size() - 1 >= caretOffsetY) {
            rows.remove(rows.size() - 1);
        }
        fitRows(screenRowSize.get());

//        String line = content.lineAt(future.pos());
//        rows.set(caretOffsetY, line);
//        if (lineCount > 1) {
//            if (future.isDeleted()) {
//                rows.remove(caretOffsetY + 1, caretOffsetY + 1 + lineCount);
//                rows.add(caretOffsetY + 1, content.lineAt(startIndex + Strings.codePointCount(line)));
//            } else {
//                rows.remove(caretOffsetY + 1);
//                List<String> adding = new ArrayList<>();
//                int index = startIndex + Strings.codePointCount(line);
//                for (int i = 1; i <= lineCount; i++) {
//                    String str = content.lineAt(index);
//                    adding.add(str);
//                    index += str.length();
//                }
//                rows.addAll(caretOffsetY + 1, adding);
//            }
//        }

        editListeners.forEach(EditListener::postEdit);

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
        return originIndex.get() + charCountOnScreen();
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

    public int caretLineCodePointLength() {
        if (caretOffsetY >= rows.size() || caretOffsetY < 0) {
            return 0;
        }
        String row = rows.get(caretOffsetY);
        return row.endsWith("\n") ? Strings.codePointCount(row) - 1 : Strings.codePointCount(row);
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

    int codePointCountOnScreen() {
        return rows.stream().mapToInt(Strings::codePointCount).sum();
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
                if (string == null) {
                    break;
                }
                i += Strings.codePointCount(string);
                rows.add(string);
            }
        }
        prepareTailRow();
    }

    private void prepareTailRow() {
        if (rows.isEmpty() ||
            (totalLines.get() > rows.size() && rows.get(rows.size() - 1).endsWith("\n"))) {
            rows.add("");
        }
    }

    private int getCaretCodePoint() {
        return (caretIndex >= content.length()) ? 0 : content.codePointAt(caretIndex);
    }


    private int getCaretPrevCodePoint() {
        int caretPosOnContent = caretIndex - 1;
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

        if (offset < 0) {
            throw new IllegalArgumentException("offset:" + offset);
        }

        int charCountOnScreen = charCountOnScreen();
        if (offset > charCountOnScreen) {
            caretOffsetY = rows.size() - 1;
            caretOffsetX = rows.get(caretOffsetY).length();
            caretOffset.set(charCountOnScreen);
            caretIndex = originIndex.get() + rows.stream().mapToInt(Strings::codePointCount).sum();
            return;
        }
        int cpLen = 0;
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            String line = rows.get(i);
            int len = line.length();
            index += len;
            if (index > offset) {
                caretOffsetY = i;
                caretOffsetX = len - (index - offset);
                caretOffset.set(offset);
                caretIndex = originIndex.get() + cpLen + Strings.codePointCount(line, 0, caretOffsetX);
                break;
            }
            cpLen += Strings.codePointCount(line);
        }
    }


    public int[] wordRange(int offset) {

        int[] ret = new int[] { offset, offset };

        // TODO offset to cp count
        int[] left  = content.untilSol(originIndex.get() + offset).toLowerCase().codePoints().toArray();
        int[] right = content.untilEol(originIndex.get() + offset).toLowerCase().codePoints().toArray();

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
        scrollMaxLines.set(Math.max(totalLines.get() - prefRowRemaining(), 0));
    }

    /**
     * Number of lines to remain when scrolling.
     * @return the number of lines to remain when scrolling
     */
    private int prefRowRemaining() {
        return (int) Math.ceil(screenRowSize.get() * 2.0 / 3.0);
    }


    void addListChangeListener(ListChangeListener<String> listener) {
        rows.addListener(listener);
    }

    void addEditListener(EditListener listener) {
        editListeners.add(listener);
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

    public Path getPath() {
        return content.path();
    }

    public boolean isDirty() {
        return content.undoSize() > 0;
    }
    public int getCaretIndex() { return caretIndex; }

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

    public String inspect() {
        return  """

            -- Debug info ---------------------------------------------------
            rows.size()   = %d
            caretOffsetY  = %d
            caretOffsetX  = %d
            caretOffset   = %d
            screenRowSize = %d

            originRowIndex = %d
            originIndex    = %d
            caretIndex     = %d

            totalLines     = %d
            scrollMaxLines = %d
            """.formatted(rows.size(), caretOffsetY, caretOffsetX, caretOffset.get(), screenRowSize.get(),
            originRowIndex.get(), originIndex.get(), caretIndex, totalLines.get(), scrollMaxLines.get());
    }

    public void reset() {
        logger.log(INFO, "== reset");
        if (content instanceof BufferedContent buffer) buffer.flush();
        caretOffsetY = caretOffsetX = 0;
        caretOffset.set(0);
        caretIndex = 0;
        originRowIndex.set(0);
        originIndex.set(0);
        if (content instanceof BufferedContent buffer) buffer.flush();
        rows.clear();
        totalLines.set(1);
        fitRows(getScreenRowSize());
    }


}

