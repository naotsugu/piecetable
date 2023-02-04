/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor2.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TextView.
 * @author Naotsugu Kobayashi
 */
public class TextView {

    /** The parent view point. */
    private final ViewPoint parent;

    /** The origin row number. */
    private final AtomicInteger originRow = new AtomicInteger();

    /** The text view buffer. */
    private final StringFigure text = new StringFigure();

    /** The caret point. */
    private LinePoint caretPoint = new LinePoint();

    /** text wrap?. */
    private boolean textWrap;


    /**
     * Constructor.
     * @param parent the parent view point
     */
    public TextView(ViewPoint parent) {
        this.parent = parent;
        this.originRow.set(0);
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
    public int originRow() { return originRow.get(); }

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

    void scrollNext() {
        CharSequence tail = parent.scrollNext(text.rowCodePointCount(0));
        text.shiftAppend(tail);
        originRow.getAndAdd(Strings.countLf(tail));
    }

    void scrollPrev() {
        CharSequence head = parent.scrollPrev(text.rowCodePointCount(text.rowSize() - 1));
        text.shiftInsert(0, head);
        originRow.getAndAdd(-Strings.countLf(head));
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
