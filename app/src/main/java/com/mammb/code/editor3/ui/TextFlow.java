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

import com.mammb.code.editor3.ui.util.PathElements;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TextFlow.
 * @author Naotsugu Kobayashi
 */
public class TextFlow extends javafx.scene.text.TextFlow {

    /** the text length. */
    private int textLength = -1;

    /** the row length. */
    private int rowLength = -1;


    /**
     * Constructor.
     */
    public TextFlow() {
        setPadding(new Insets(4));
        setTabSize(4);
    }


    /**
     * Set the text list.
     * @param texts the text list
     */
    void setAll(List<Text> texts) {
        getChildren().setAll(texts);
        clearCacheValue();
    }


    /**
     * Gets whether the display can be moved to the next line.
     * @return {@code true} if the display can be moved to the next line
     */
    public boolean canTranslateRowNext() {
        double remaining = getBoundsInLocal().getHeight() - getLayoutBounds().getHeight()
            + getTranslateY();
        return remaining >= visuallyHeadRowHeight();
    }


    /**
     * Display moves to the next line.
     */
    public void translateRowNext() {
        setTranslateY(getTranslateY() - visuallyHeadRowHeight());
    }


    /**
     * Gets whether the display can be moved to the previous line.
     * @return {@code true} if the display can be moved to the previous line
     */
    public boolean canTranslateRowPrev() {
        return getTranslateY() < 0;
    }


    /**
     * Display moves to the previous line.
     */
    public void translateRowPrev() {
        if (getTranslateY() == 0) return;
        double height = PathElements.height(caretShape(
            insertionIndexAt(0, getTranslateY() - 1), true));
        setTranslateY(getTranslateY() + height);
    }


    public String[] rowAt(int physicalRow) {
        int[] indexes = rowOffset(physicalRow);
        if (indexes[0] < 0 || indexes[1] < 0) {
            return new String[0];
        }
        List<String> ret = new ArrayList<>();
        String text = text();
        int startIndex = indexes[0];
        for (;;) {
            double y = PathElements.getY(caretShape(startIndex, true)[0]);
            int endIndex = insertionIndexAt(Double.MAX_VALUE, y) + 1;
            ret.add(text.substring(startIndex, endIndex));
            if (endIndex >= indexes[1]) {
                break;
            }
            startIndex = endIndex;
        }
        return ret.toArray(new String[0]);
    }


    /**
     * Get the insertion index.
     * @param x the specified point x
     * @param y the specified point y
     * @return the index of the insertion position
     */
    public int insertionIndexAt(double x, double y) {
        HitInfo hit = hitTest(new Point2D(x, y));
        return hit.getInsertionIndex();
    }


    /**
     * Get the text length.
     * @return the text length
     */
    public int textLength() {
        if (textLength == -1) {
            textLength = getChildren().stream()
                .filter(Text.class::isInstance).map(Text.class::cast)
                .map(Text::getText).mapToInt(String::length).sum();
        }
        return textLength;
    }


    /**
     * Get the row size.
     * <pre>
     *     """
     *     abc"""  -> row size : 1
     * </pre>
     * <pre>
     *     """
     *     abc
     *     """  -> row size : 2
     * </pre>
     * @return the row size
     */
    public int logicalRowSize() {
        if (rowLength == -1) {
            rowLength = 0;
            double y = 0;
            double prevTop = -1;
            while (true) {
                PathElement[] pe = caretShape(insertionIndexAt(0, y), true);
                if (pe.length < 2) break;
                double top    = ((MoveTo) pe[0]).getY();
                double bottom = ((LineTo) pe[1]).getY();
                if (top == prevTop) break;
                rowLength++;
                prevTop = top;
                y = bottom + 1;
            }
        }
        return rowLength;
    }


    /**
     * Get the size of head row.
     * @return the size of head row
     */
    private double visuallyHeadRowHeight() {
        return PathElements.height(caretShape(
            insertionIndexAt(0, getTranslateY()), true));
    }


    /**
     * Get the text content.
     * @return the text content
     */
    private String text() {
        return getChildren().stream()
            .filter(Text.class::isInstance).map(Text.class::cast)
            .map(Text::getText).collect(Collectors.joining());
    }


    /**
     * Get the row index pair(start inclusive, end inclusive).
     * @param physicalRow number of row
     * @return the row index pair(start, end)
     */
    private int[] rowOffset(int physicalRow) {

        int[] ret = new int[] { -1, -1 };

        int index = 0;
        for (Node node : getChildren()) {
            if (node instanceof Text text) {
                String str = text.getText();
                for (int i = 0; i < str.length(); i++, index++) {
                    if (physicalRow == 0 && ret[0] == -1) {
                        ret[0] = index;
                    }
                    if (str.charAt(i) == '\n') {
                        if (ret[0] > -1) {
                            ret[1] = index;
                            return ret;
                        }
                        physicalRow--;
                    }
                }
            }
        }
        return new int[] { 0, index };
    }


    /**
     * Clear cache value.
     */
    private void clearCacheValue() {
        textLength = -1;
        rowLength = -1;
    }

}
