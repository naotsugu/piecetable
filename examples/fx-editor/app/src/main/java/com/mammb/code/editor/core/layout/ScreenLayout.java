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
 * The screen layout.
 * @author Naotsugu Kobayashi
 */
public interface ScreenLayout extends LineLayout {

    void setScreenSize(double width, double height);
    void scrollNext(int lineDelta);
    void scrollPrev(int lineDelta);
    void scrollAt(int line);
    void scrollX(double x);
    void refreshBuffer(int startRow, int endRow);
    List<Text> texts();
    List<Text> lineNumbers();
    Optional<Loc> locationOn(int row, int col);
    int yToLineOnScreen(double y);
    double screenWidth();
    double screenHeight();
    int screenLineSize();
    int topLine();
    void applyScreenScroll(ScreenScroll screenScroll);

    static ScreenLayout of(Content content, FontMetrics fm) {
        ContentLayout layout = new RowLayout(content, fm);
        return new BasicScreenLayout(layout);
    }

    static ScreenLayout wrapOf(Content content, FontMetrics fm) {
        ContentLayout layout = new WrapLayout(content, fm);
        return new BasicScreenLayout(layout);
    }

    class BasicScreenLayout implements ScreenLayout {
        private double screenWidth = 0, screenHeight = 0;
        private double xShift = 0;
        private double xMax = 0;
        private int topLine = 0;
        private final List<Text> buffer = new ArrayList<>();
        private final ContentLayout layout;

        private BasicScreenLayout(ContentLayout layout) {
            this.layout = layout;
        }

        @Override
        public void setScreenSize(double width, double height) {
            layout.setScreenWidth(width);
            this.screenWidth = width;
            this.screenHeight = height;
            fillBuffer();
        }

        @Override
        public void scrollNext(int lineDelta) {
            scrollAt(topLine + lineDelta);
        }

        @Override
        public void scrollPrev(int lineDelta) {
            scrollAt(topLine - lineDelta);
        }

        @Override
        public void scrollAt(int line) {
            line = Math.clamp(line, 0, layout.lineSize() - 1);
            int delta = line - topLine;
            if (delta == 0) return;
            if (Math.abs(delta) < screenLineSize() * 2 / 3) {
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
        public Text rowTextAt(int row) {
            return layout.rowTextAt(row);
        }

        @Override
        public List<Text> lineNumbers() {
            List<Text> ret = new ArrayList<>();
            int prev = -1;
            for (Text text : buffer) {
                if (text.row() >= rowSize()) {
                    break;
                } if (text.row() == prev) {
                    ret.add(Text.of(text.row(), "", new double[0], text.height()));
                } else {
                    String num = String.valueOf(text.row() + 1);
                    double[] advances = new double[num.length()];
                    Arrays.fill(advances, layout.standardCharWidth());
                    ret.add(Text.of(text.row(), num, advances, text.height()));
                }
            }
            return ret;
        }

        @Override
        public Optional<Loc> locationOn(int row, int col) {
            return layout.loc(row, col, topLine, topLine + screenLineSize())
                    .map(loc -> new Loc(loc.x(), loc.y() - yOnLayout(topLine)));
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
        public double yOnLayout(int line) {
            return layout.yOnLayout(line);
        }

        @Override
        public double xOnLayout(int line, int col) {
            return layout.xOnLayout(line, col);
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
        public int yToLineOnScreen(double y) {
            return Math.clamp(topLine + (int) (y / layout.lineHeight()), 0, lineSize() - 1);
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
        public double screenWidth() {
            return screenWidth;
        }

        @Override
        public double screenHeight() {
            return screenHeight;
        }

        @Override
        public int screenLineSize() {
            return (int) Math.ceil(Math.max(0, screenHeight) / layout.lineHeight());
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
            scroll.vertical(0, layout.lineSize() - 1, topLine, screenLineSize());
            double max = xMax - Math.min(xMax, screenWidth / 2);
            scroll.horizontal(0, max, xShift, screenWidth * max / xMax);
        }

        private void fillBuffer() {
            buffer.clear();
            buffer.addAll(layout.texts(topLine, topLine + screenLineSize()));
            buffer.stream().mapToDouble(Text::width)
                    .filter(w -> w > xMax).max()
                    .ifPresent(w -> xMax = w);
        }

    }
}
