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
import com.mammb.code.editor.core.ScreenScroll;
import com.mammb.code.editor.core.text.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The LayoutView.
 * @author Naotsugu Kobayashi
 */
public interface LayoutView {

    int rowToFirstLine(int row);
    int rowToLastLine(int row);
    int rowToLine(int row, int col);
    int lineToRow(int line);
    int lineSize();
    int rowSize();

    void setSize(double width, double height);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int line);
    void scrollX(double x);
    void refreshBuffer(int startRow, int endRow);
    List<Text> texts();
    Text text(int line);
    Text textAt(int row);
    List<Text> lineNumbers();
    Optional<Loc> locationOn(int row, int col);
    double lineToYOnLayout(int line);
    double lineToYOnView(int line);
    double colToXOnLayout(int line, int col);
    double colToXOnView(int line, int col);
    int xToCol(int line, double x);
    int yToLineOnView(double y);
    int homeColOnRow(int line);
    int endColOnRow(int line);
    double width();
    double height();
    int lineSizeOnView();
    double lineHeight();
    int topLine();
    void applyScreenScroll(ScreenScroll screenScroll);

    static LayoutView of(Content content, FontMetrics fm) {
        Layout layout = new RowLayout(content, fm);
        return new BasicLayoutView(layout);
    }

    static LayoutView wrapOf(Content content, FontMetrics fm) {
        Layout layout = new WrapLayout(content, fm);
        return new BasicLayoutView(layout);
    }

    class BasicLayoutView implements LayoutView {
        private double width = 0, height = 0;
        private double xShift = 0;
        private double xMax = 0;
        private int topLine = 0;
        private final List<Text> buffer = new ArrayList<>();
        private final Layout layout;

        public BasicLayoutView(Layout layout) {
            this.layout = layout;
        }

        @Override
        public void setSize(double width, double height) {
            layout.setWidth(width);
            this.width = width;
            this.height = height;
            fillBuffer();
        }

        @Override
        public void scrollNext(int delta) {
            scrollAt(topLine + delta);
        }

        @Override
        public void scrollPrev(int delta) {
            scrollAt(topLine - delta);
        }

        @Override
        public void scrollAt(int line) {
            line = Math.clamp(line, 0, layout.lineSize() - 1);
            int delta = line - topLine;
            if (delta == 0) return;
            if (Math.abs(delta) < lineSizeOnView() * 2 / 3) {
                topLine = line;
                List<Text> texts;
                if (delta > 0) {
                    // scroll next
                    buffer.subList(0, delta).clear();
                    texts = layout.texts(line + buffer.size(), line + buffer.size() + delta);
                    buffer.addAll(texts);
                } else {
                    // scroll prev
                    buffer.subList(buffer.size() + delta, buffer.size()).clear();
                    texts = layout.texts(line, line - delta);
                    buffer.addAll(0, texts);
                }
                texts.stream().mapToDouble(Text::width)
                        .filter(w -> w > xMax).max()
                        .ifPresent(w -> xMax = w);
            } else {
                this.topLine = line;
                fillBuffer();
            }
        }

        @Override
        public void scrollX(double x) {
            xShift = x;
        }

        @Override
        public void refreshBuffer(int startRow, int endRow) {
            layout.refreshAt(startRow, endRow);
            fillBuffer();// TODO optimize
        }

        @Override
        public List<Text> texts() {
            return buffer;
        }

        @Override
        public Text text(int line) {
            return layout.text(line);
        }

        @Override
        public Text textAt(int row) {
            return layout.rowTextAt(row);
        }

        @Override
        public List<Text> lineNumbers() {
            List<Text> ret = new ArrayList<>();
            int prev = -1;
            double w = layout.fontMetrics().getAdvance("0");
            for (Text text : buffer) {
                if (text.row() == prev) {
                    ret.add(Text.of(text.row(), "", new double[0], text.height()));
                } else {
                    String num = String.valueOf(text.row());
                    double[] advances = new double[num.length()];
                    Arrays.fill(advances, w);
                    ret.add(Text.of(text.row(), num, advances, text.height()));
                }
            }
            return ret;
        }

        @Override
        public Optional<Loc> locationOn(int row, int col) {
            return layout.loc(row, col, topLine, topLine + lineSizeOnView())
                    .map(loc -> new Loc(loc.x(), loc.y() - lineToYOnLayout(topLine)));
        }

        @Override
        public double lineToYOnLayout(int line) {
            return layout.y(line);
        }

        @Override
        public double lineToYOnView(int line) {
            return lineToYOnLayout(line) - lineToYOnLayout(topLine);
        }

        @Override
        public int lineSize() {
            return layout.lineSize();
        }

        @Override
        public int rowSize() {
            return layout.rowSize();
        }

        @Override
        public double colToXOnLayout(int line, int col) {
            return layout.x(rowToLine(line, col), col);
        }

        @Override
        public double colToXOnView(int line, int col) {
            return colToXOnLayout(line, col) - xShift;
        }

        @Override
        public int rowToFirstLine(int row) {
            return layout.rowToFirstLine(row);
        }

        @Override
        public int rowToLastLine(int row) {
            return layout.rowToLastLine(row);
        }

        @Override
        public int rowToLine(int row, int col) {
            return layout.rowToLine(row, col);
        }

        @Override
        public int lineToRow(int line) {
            return layout.lineToRow(line);
        }

        @Override
        public int xToCol(int line, double x) {
            return layout.xToCol(line, x);
        }

        @Override
        public int yToLineOnView(double y) {
            return topLine + (int) (y / layout.lineHeight());
        }

        @Override
        public int homeColOnRow(int line) {
            return layout.homeColOnRow(line);
        }

        @Override
        public int endColOnRow(int line) {
            return layout.endColOnRow(line);
        }

        @Override
        public double width() {
            return width;
        }

        @Override
        public double height() {
            return height;
        }

        @Override
        public int lineSizeOnView() {
            return (int) Math.ceil(Math.max(0, height) / layout.lineHeight());
        }

        @Override
        public double lineHeight() {
            return layout.lineHeight();
        }

        @Override
        public int topLine() {
            return topLine;
        }

        @Override
        public void applyScreenScroll(ScreenScroll scroll) {
            scroll.vertical(0, layout.lineSize() - 1, topLine, lineSizeOnView());
            double max = xMax - Math.min(xMax, width / 2);
            scroll.horizontal(0, max, xShift, width * max / xMax);
        }

        private void fillBuffer() {
            buffer.clear();
            buffer.addAll(layout.texts(topLine, topLine + lineSizeOnView()));
            buffer.stream().mapToDouble(Text::width)
                    .filter(w -> w > xMax).max()
                    .ifPresent(w -> xMax = w);
        }

    }
}
