/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.core.layout;

import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.text.RowText;
import com.mammb.code.editor.core.text.Text;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * The RowLayout.
 * @author Naotsugu Kobayashi
 */
public class RowLayout implements Layout {

    private final double lineHeight;
    private final Content content;
    private final FontMetrics fm;

    public RowLayout(Content content, FontMetrics fm) {
        this.lineHeight = fm.getLineHeight();
        this.content = content;
        this.fm = fm;
    }

    public void setWidth(double width) {
        // nothing to do
    }

    public void refresh(int line) {
        // nothing to do
    }

    @Override
    public void refreshAt(int startRow, int endRow) {
        // nothing to do
    }

    @Override
    public Text text(int line) {
        return rowText(line);
    }

    @Override
    public List<Text> texts(int startLine, int endLine) {
        return IntStream.range(startLine, endLine)
                .mapToObj(this::rowText).map(Text.class::cast).toList();
    }

    @Override
    public RowText rowText(int line) {
        return rowTextAt(line);
    }

    @Override
    public RowText rowTextAt(int row) {
        return RowText.of(row, content.getText(row), fm);
    }

    @Override
    public double lineHeight() {
        return lineHeight;
    }

    @Override
    public int xToCol(int line, double x) {
        return text(line).indexTo(x);
    }

    @Override
    public int homeColOnRow(int line) {
        return 0;
    }

    @Override
    public FontMetrics fontMetrics() {
        return fm;
    }

    @Override
    public int lineSize() {
        return rowSize();
    }

    @Override
    public int rowSize() {
        return content.rows();
    }

    @Override
    public int rowToFirstLine(int row) {
        return Math.clamp(row, 0, lineSize());
    }

    @Override
    public int rowToLastLine(int row) {
        return Math.clamp(row, 0, lineSize());
    }

    @Override
    public int lineToRow(int line) {
        return Math.clamp(line, 0, rowSize());
    }

    @Override
    public int rowToLine(int row, int col) {
        return Math.clamp(row, 0, lineSize());
    }

    @Override
    public Optional<Loc> loc(int row, int col, int startLine, int endLine) {
        if (startLine <= row && row < endLine) {
            return Optional.of(new Loc(x(row, col),y(row)));
        } else {
            return Optional.empty();
        }
    }

}
