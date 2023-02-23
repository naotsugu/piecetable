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
package com.mammb.code.editor3.ui.util;

import javafx.geometry.Point2D;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The text metrics.
 * @author Naotsugu Kobayashi
 */
public class TextMetrics {

    public record Line(int rowIndex, int offset, int length, double height) { }


    private final List<Line> lines;


    /**
     * Constructor.
     * @param flow the text flow
     */
    private TextMetrics(TextFlow flow) {
        lines = createLines(Objects.requireNonNull(flow));
    }


    /**
     * Create the TextMetrics from given the text flow.
     * @param flow the text flow
     * @return the created TextMetrics
     */
    public static TextMetrics of(TextFlow flow) {
        return new TextMetrics(flow);
    }



    private List<Line> createLines(TextFlow flow) {

        final List<Line> lines = new ArrayList<>();
        final String text = text(flow);

        int row = 0;
        int offset = 0;
        double y = 0;

        while (offset < text.length()) {
            int tail = flow.hitTest(new Point2D(Double.MAX_VALUE, y + 1)).getInsertionIndex();
            double height = height(flow.caretShape(tail, true));
            int lf = (tail < text.length() && text.charAt(tail) == '\n') ? 1 : 0;
            tail += lf;
            lines.add(new Line(row, offset, tail - offset, height));
            row += lf;
            offset = tail;
            y += height;
        }
        return lines;
    }


    /**
     * Get the lines.
     * @return the lines
     */
    public List<Line> lines() { return lines; }


    /**
     * Get the height of path.
     * @param elements the path elements
     * @return the height of path
     */
    private double height(PathElement[] elements) {
        return ((LineTo) elements[1]).getY() - ((MoveTo) elements[0]).getY();
    }


    /**
     * Get the text content.
     * @param flow the text flow
     * @return the text content
     */
    private String text(TextFlow flow) {
        return flow.getChildren().stream()
            .filter(Text.class::isInstance).map(Text.class::cast)
            .map(Text::getText).collect(Collectors.joining());
    }

}
