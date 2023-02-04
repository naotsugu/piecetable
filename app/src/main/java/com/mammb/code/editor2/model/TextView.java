package com.mammb.code.editor2.model;

/**
 * TextView.
 * @author Naotsugu Kobayashi
 */
public class TextView {

    /** The origin row number. */
    private int originRow;

    /** The text view buffer. */
    private final StringFigure text = new StringFigure();

    /** The caret point. */
    private LinePoint caretPoint = new LinePoint();

    /** text wrap?. */
    private boolean textWrap;

    /** The edit event listener. */
    private EventListener<Edit> editListener;


    /**
     * Constructor.
     * @param editListener the edit event listener
     */
    public TextView(EventListener<Edit> editListener) {
        this.editListener = editListener;
        this.originRow = 0;
        this.textWrap = false;
    }

    /**
     * Get the row size of this view text.
     * @return the row size
     */
    public int rowSize() {
        return text.rowSize();
    }

    /**
     * Returns the number of Unicode code points in the specified range of this view text.
     * @param beginIndex the index to the first char of the text range
     * @param endIndex the index after the last char of the text range
     * @return the number of Unicode code points in the specified text range
     */
    public int codePointCount(int beginIndex, int endIndex) {
        return text.codePointCount(beginIndex, endIndex);
    }

    /**
     * Get the origin row number.
     * @return the origin row number
     */
    public int originRow() { return originRow; }

    /**
     * Get the code point count.
     * @return the code point count
     */
    public int codePointCount() {
        return text.codePointCount();
    }

    /**
     * Gets whether to wrap text.
     * @return {@code true} if text is to be wrapped.
     */
    public boolean isTextWrap() { return textWrap; }

    /**
     * Get the text string.
     * @return the text string
     */
    public String string() {
        return text.toString();
    }

    // ------------------------------------------------------------------------

    /**
     * Get the caret index.
     * @return the caret index
     */
    private int caretIndex() {
        return text.rowIndex(caretPoint.row()) + caretPoint.offset();
    }

    int scrollNext(CharSequence tail) {
        int deleted = text.shiftAppend(tail);
        originRow += Strings.countLf(tail);
        return deleted;
    }

    int scrollPrev(CharSequence head) {
        int deleted = text.shiftInsert(0, head);
        originRow -= Strings.countLf(head);
        return deleted;
    }

    private void moveToCaretVisible() {

    }

    public void caretNext() {
        moveToCaretVisible();
        int nextIndex = caretIndex() + 1;
        if (nextIndex >= text.length()) return;
        if (text.charAt(nextIndex) == '\n') {
            caretPoint.toNextHead();
        } else {
            caretPoint.toNext();
        }
    }

    public void caretPrev() {
        moveToCaretVisible();
        int prevIndex = caretIndex() - 1;
        if (prevIndex <= 0) return;
        if (text.charAt(prevIndex) == '\n') {
            caretPoint.toPrevTail(text.rowLength(caretPoint.row() - 1));
        } else {
            caretPoint.toPrev();
        }
    }

    public void caretNextRow() {
        moveToCaretVisible();
        if (textWrap) {

        } else {
            if (caretPoint.row() == rowSize() - 1) return;
            caretPoint.toNextRow();
        }
    }

    public void caretPrevRow() {
        moveToCaretVisible();
        if (textWrap) {

        } else {
            if (caretPoint.row() == 0) return;
            caretPoint.toPrevRow();
        }
    }

}
