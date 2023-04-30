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
package com.mammb.code.editor.ui;

import com.mammb.code.editor.ui.util.TextMetrics;
import com.mammb.code.editor.ui.util.Texts;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import java.util.List;

/**
 * TextFlow.
 * Extended {@link javafx.scene.text.TextFlow} components.
 * @author Naotsugu Kobayashi
 */
public class TextFlow extends javafx.scene.text.TextFlow implements ScreenText {

    /** The text metrics. */
    private TextMetrics metrics;

    /** The translated line(not a row) offset. */
    private int translatedLineOffset = 0;

    /** textWrap?. */
    private boolean textWrap = true;


    /**
     * Constructor.
     */
    public TextFlow() {
        setPadding(new Insets(0, 0, 0, 4));
        setTabSize(4);
        setCursor(Cursor.TEXT);
        addTextIfEmpty();
    }


    /**
     * Set the text list.
     * @param texts the text list
     */
    public void setAll(List<Text> texts) {
        getChildren().setAll(texts);
        addTextIfEmpty();
        metrics = null;
        if (!textWrap) {
            setWidthBulk(TextMetrics.of(this).maxRowWidth() +
                getPadding().getLeft() + getPadding().getRight());
        }
    }


    /**
     * Display moves to the next line.
     */
    public void translateLineNext() {
        if (translatedLineOffset >= metrics().lines().size() - 1) {
            return;
        }
        setTranslateY(getTranslateY() -
            metrics().lines().get(translatedLineOffset).height());
        translatedLineOffset += 1;
    }


    /**
     * Display moves to the previous line.
     */
    public void translateLinePrev() {
        if (translatedLineOffset == 0) {
            return;
        }
        translatedLineOffset -= 1;
        setTranslateY(getTranslateY() +
            metrics().lines().get(translatedLineOffset).height());
    }


    /**
     * Clear translate
     */
    public void clearTranslation() {
        translatedLineOffset = 0;
        setTranslateY(0);
        setTranslateX(0);
    }


    @Override
    public int insertionIndexAt(double x, double y) {
        return hitTest(new Point2D(x, y)).getInsertionIndex();
    }


    /**
     * Get the size of line at the specified offset of row.
     * @param rowOffset the specified offset of row
     * @return the size of line
     */
    public int lineSize(final int rowOffset) {
        return (int) metrics().lines().stream()
            .filter(l -> l.rowIndex() == rowOffset)
            .count();
    }


    /**
     * Get the row index pair(start inclusive, end exclusive).
     * int[0] sol index
     * int[1] eol index (index of '\n' + 1, maybe)
     * @param physicalRow number of row
     * @return the row index pair(start, end)
     */
    int[] rowOffset(final int physicalRow) {

        List<TextMetrics.Line> line = metrics().lines().stream()
            .filter(l -> l.rowIndex() == physicalRow)
            .toList();

        int sol = line.stream().mapToInt(TextMetrics.Line::offset).min().orElse(-1);
        return new int[] {
            sol, sol + line.stream().mapToInt(TextMetrics.Line::length).sum()
        };

    }


    @Override
    public char charAt(int index) {
        return metrics().textString().charAt(index);
    }


    /**
     * Get the character sequence that is a subsequence of this text flow.
     * @param beginIndex the beginning index, inclusive
     * @param endIndex the end index, exclusive
     * @return the character sequence
     */
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return metrics().textString().subSequence(beginIndex, endIndex);
    }


    @Override
    public int textLength() {
        return metrics().textLength();
    }


    /**
     * Get the total height of text.
     * @return the total height of text
     */
    public double totalHeight() {
        return metrics().totalHeight();
    }


    /**
     * Get the number of lines containing wrapped text.
     * @return the number of lines containing wrapped text
     */
    public int lineSize() {
        return metrics().lines().size();
    }


    /**
     * Get the row length.
     * @return the row length
     */
    public int rowSize() {
        return metrics().rowSize();
    }


    /**
     * Get the number of wrapped lines.
     * i.e. {@code lineSize() - rowSize()}
     * @return the number of wrapped lines
     */
    public int wrappedLines() {
        return lineSize() - rowSize();
    }


    @Override
    public PathElement[] caretShape(int charIndex, boolean leading) {
        if (charIndex < 0) {
            return new PathElement[] { new MoveTo(-1, -1), new LineTo(-1, -1) };
        }
        return super.caretShape(charIndex, leading);
    }


    /**
     * The translated row offset.
     * @return the translated row offset
     */
    public int translatedLineOffset() { return translatedLineOffset; }


    /**
     * Get the translated shift row.
     * @return the translated shift row
     */
    public int translatedShiftRow() {
        return metrics().lines().get(translatedLineOffset).rowIndex();
    }


    /**
     * Toggle text wrap.
     */
    public void toggleTextWrap() {
        setTextWrap(!textWrap);
    }


    /**
     * Set text wrap.
     * @param wrap the text wrap
     */
    void setTextWrap(boolean wrap) {
        if (textWrap == wrap) return;
        textWrap = wrap;
        double width = metrics().maxRowWidth();
        metrics = null;
        clearTranslation();
        if (textWrap) {
            setWidthBulk(Region.USE_COMPUTED_SIZE);
        } else {
            setWidthBulk(width + getPadding().getLeft() + getPadding().getRight());
        }
    }


    /**
     * Get the text metrics.
     * @return the text metrics
     */
    TextMetrics metrics() {
        TextMetrics value = metrics;
        if (metrics == null) {
            metrics = value = TextMetrics.of(this);
        }
        return value;
    }


    /**
     * Set width.
     * @param width size of width
     */
    private void setWidthBulk(double width) {
        setMinWidth(width);
        setMaxWidth(width);
        setPrefWidth(width);
        if (width == Region.USE_COMPUTED_SIZE && getParent() != null) {
            setWidth(getParent().getLayoutBounds().getWidth());
        } else {
            setWidth(width);
        }
    }


    /**
     * Add empty text.
     */
    private void addTextIfEmpty() {
        if (getChildren().isEmpty()) {
            getChildren().add(Texts.asText(""));
        }
    }


    @Override
    public String toString() {
        return "TextFlow{" +
            "metrics=" + metrics +
            ", translatedOffset=" + translatedLineOffset +
            '}';
    }

}
