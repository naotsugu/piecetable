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
package com.mammb.code.editor.ui.util;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.mammb.code.editor.ui.util.TextMetrics.Line;

/**
 * The test of {@link TextMetrics}.
 * @author Naotsugu Kobayashi
 */
class TextMetricsTest {

    private static final double defaultHeight = new Text("x").getLayoutBounds().getHeight();

    @Test
    void testSingleLine() {
        Text text = new Text("abc");
        TextFlow flow = new TextFlow(text);
        TextMetrics tm = TextMetrics.of(flow);
        List<Line> lines = tm.lines();

        assertEquals(1, lines.size());
        assertEquals(new Line(0, 0, 3, defaultHeight, PathElements.width(text.rangeShape(0, 3))), lines.get(0));
    }


    @Test
    void testMultiRow() {
        Text text = new Text("abc\ndef");
        TextFlow flow = new TextFlow(text);
        TextMetrics tm = TextMetrics.of(flow);
        List<Line> lines = tm.lines();

        assertEquals(2, lines.size());
        assertEquals(new Line(0, 0, 4, defaultHeight, PathElements.width(text.rangeShape(0, 3))), lines.get(0));
        assertEquals(new Line(1, 4, 3, defaultHeight, PathElements.width(text.rangeShape(4, 7))), lines.get(1));
    }


    @Test
    void testMultiRowEndWithLf() {
        Text text = new Text("abc\ndef\n");
        TextFlow flow = new TextFlow(text);
        TextMetrics tm = TextMetrics.of(flow);
        List<Line> lines = tm.lines();

        assertEquals(3, lines.size());
        assertEquals(new Line(0, 0, 4, defaultHeight, PathElements.width(text.rangeShape(0, 3))), lines.get(0));
        assertEquals(new Line(1, 4, 4, defaultHeight, PathElements.width(text.rangeShape(4, 7))), lines.get(1));
        assertEquals(new Line(2, 8, 0, defaultHeight, PathElements.width(text.rangeShape(7, 8))), lines.get(2));
    }

}
