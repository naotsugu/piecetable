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

/**
 * TextView.
 * @author Naotsugu Kobayashi
 */
public class TextView {

    /** The origin row number. */
    private int originRow;

    /** row height. */
    private int height;

    /** The text view buffer. */
    private final StringFigure text = new StringFigure();

    /** The text source. */
    private final TextSource source;

    /** The caret point. */
    private final LinePoint caretPoint = new LinePoint();

    /** text wrap?. */
    private boolean textWrap;

    /** The scroll behavior. */
    private ScrollBehavior scrollBehavior;

    /** The caret behavior. */
    private CaretBehavior caretBehavior;


    /**
     * Constructor.
     * @param source the text source
     */
    public TextView(TextSource source) {
        this.originRow = 0;
        this.height = 10;
        this.source = source;
        this.textWrap = false;
        this.scrollBehavior = this.textWrap
            ? new ScrollBehavior.WrapScrollBehavior(source, this)
            : new ScrollBehavior.NowrapScrollBehavior(source, this);
        this.caretBehavior = this.textWrap
            ? new CaretBehavior.WrapCaretBehavior(this)
            : new CaretBehavior.NowrapCaretBehavior(this);
        fillText();
    }

    public void fillText() {
        text.set(source.rows(height));
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
     * Set the origin row number.
     * @param originRow the origin row number
     */
    void setOriginRow(int originRow) {
        this.originRow = originRow;
    }

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

    /**
     * Get the scroll behavior.
     * @return the scroll behavior
     */
    public ScrollBehavior scrollBehavior() {
        return scrollBehavior;
    }

    /**
     * Get the caret behavior.
     * @return the caret behavior
     */
    public CaretBehavior caretBehavior() {
        return caretBehavior;
    }

    // ------------------------------------------------------------------------

    /**
     * Get the text view buffer.
     * @return the text view buffer
     */
    StringFigure text() {
        return text;
    }

    /**
     * Get the caret index.
     * @return the caret index
     */
    int caretIndex() {
        return text.rowIndex(caretPoint.row()) + caretPoint.offset();
    }

    /**
     * Get the caret point.
     * @return the caret point
     */
    LinePoint caretPoint() {
        return caretPoint;
    }

    private void moveToCaretVisible() {

    }

}
