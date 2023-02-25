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
package com.mammb.code.editor3.ui;

import com.mammb.code.editor3.ui.util.TextMetrics;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import java.util.List;

/**
 * TextFlow.
 * @author Naotsugu Kobayashi
 */
public class TextFlow extends javafx.scene.text.TextFlow {

    /** The text metrics. */
    private TextMetrics metrics;

    /** The translated offset. */
    private int translatedOffset = 0;


    /**
     * Constructor.
     */
    public TextFlow() {
        setPadding(new Insets(0, 0, 0, 4));
        setTabSize(4);
        setCursor(Cursor.TEXT);
    }


    /**
     * Set the text list.
     * @param texts the text list
     */
    public void setAll(List<Text> texts) {
        getChildren().setAll(texts);
        metrics = null;
    }


    /**
     * Gets whether the display can be moved to the next line.
     * @return {@code true} if the display can be moved to the next line
     */
    public boolean canTranslateRowNext() {
        double remaining = getBoundsInLocal().getHeight()
            - getParent().getLayoutBounds().getHeight()
            + getTranslateY();
        return remaining > visuallyHeadRowHeight();
    }


    /**
     * Display moves to the next line.
     */
    public void translateRowNext() {
        setTranslateY(getTranslateY() - metrics().lines().get(translatedOffset).height());
        translatedOffset += 1;
    }


    /**
     * Gets whether the display can be moved to the previous line.
     * @return {@code true} if the display can be moved to the previous line
     */
    public boolean canTranslateRowPrev() {
        return translatedOffset > 0;
    }


    /**
     * Display moves to the previous line.
     */
    public void translateRowPrev() {
        if (translatedOffset == 0) {
            return;
        }
        translatedOffset -= 1;
        setTranslateY(getTranslateY() + metrics().lines().get(translatedOffset).height());
    }


    /**
     * Clear translate
     */
    public void clearTranslation() {
        translatedOffset = 0;
        setTranslateY(0);
    }


    /**
     * Get the insertion index.
     * @param x the specified point x
     * @param y the specified point y
     * @return the index of the insertion position
     */
    public int insertionIndexAt(double x, double y) {
        return hitTest(new Point2D(x, y)).getInsertionIndex();
    }


    /**
     * Get the height of the first row of the visible area.
     * If text is text-wrapped, it will be the height of multiple lines.
     * <pre>
     *     xxxxxxxxxxxxxxx
     *     ----------------
     *    |ooooooooooooooo|
     *    |oooo$          |
     *    |               |
     * </pre>
     * @return the height of the first row of the visible area
     */
    private double visuallyHeadRowHeight() {
        double height = 0;
        List<TextMetrics.Line> lines = metrics().lines();
        for (int i = translatedOffset; i < lines.size(); i++) {
            TextMetrics.Line line = lines.get(i);
            height += line.height();
            if (line.rowIndex() != lines.get(translatedOffset).rowIndex()) {
                break;
            }
        }
        return height;
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
            .filter(l -> l.rowIndex() == physicalRow).toList();

        int sol = line.stream().mapToInt(TextMetrics.Line::offset).min().orElse(-1);
        return new int[] {
            sol, sol + line.stream().mapToInt(TextMetrics.Line::length).sum()
        };

    }


    /**
     * Get the char value at the specified index.
     * The first char value is at index 0.
     * @param index the index of the char value
     * @return the char value at the specified index of this string.
     */
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


    /**
     * Get the text length.
     * @return the text length
     */
    public int textLength() {
        return metrics().textLength();
    }


    @Override
    public PathElement[] caretShape(int charIndex, boolean leading) {
        if (charIndex < 0) {
            return new PathElement[] { new MoveTo(-1, -1), new LineTo(-1, -1) };
        }
        return super.caretShape(charIndex, leading);
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
     * Get the translated shift row.
     * @return the translated shift row
     */
    public int translatedShiftRow() {
        return metrics().lines().get(translatedOffset).rowIndex();
    }

}
