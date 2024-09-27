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
package com.mammb.code.editor.core.model;

import com.mammb.code.editor.core.Caret;
import com.mammb.code.editor.core.CaretGroup;
import com.mammb.code.editor.core.Content;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.EditorModel;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.ScreenScroll;
import com.mammb.code.editor.core.Theme;
import com.mammb.code.editor.core.layout.LayoutView;
import com.mammb.code.editor.core.layout.Loc;
import com.mammb.code.editor.core.syntax.Syntax;
import com.mammb.code.editor.core.text.Style;
import com.mammb.code.editor.core.text.StyledText;
import com.mammb.code.editor.core.text.Text;
import com.mammb.code.editor.core.Caret.Point;
import com.mammb.code.editor.core.Caret.Range;
import javafx.scene.input.DataFormat;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlainEditorModel implements EditorModel {

    private double marginTop = 5, marginLeft = 70;
    private final Content content;
    private final LayoutView view;
    private final Syntax syntax;
    private final CaretGroup carets = CaretGroup.of();
    private final ScreenScroll scroll;

    public PlainEditorModel(Content content, FontMetrics fm, Syntax syntax, ScreenScroll scroll) {
        this.content = content;
        this.view = LayoutView.of(content, fm);
        this.syntax = syntax;
        this.scroll = scroll;
    }

    @Override
    public void draw(Draw draw) {
        view.applyScreenScroll(scroll);
        draw.clear();
        for (Range r : carets.marked()) {
            Loc l1 = view.locationOn(r.min().row(), r.min().col()).orElse(null);
            Loc l2 = view.locationOn(r.max().row(), r.max().col()).orElse(null);
            if (l1 == null && l2 == null) break;
            if (l1 == null) l1 = new Loc(0, 0);
            if (l2 == null) l2 = new Loc(view.width(), view.height());
            draw.select(
                    l1.x() + marginLeft - scroll.xVal(), l1.y() + marginTop,
                    l2.x() + marginLeft - scroll.xVal(), l2.y() + marginTop,
                    marginLeft - scroll.xVal(), view.width() + marginLeft);
        }
        double y = 0;
        for (Text text : view.texts()) {
            double x = 0;
            var spans = syntax.apply(text.row(), text.value());
            for (StyledText st : StyledText.of(text).putAll(spans).build()) {
                draw.text(st.value(), x + marginLeft - scroll.xVal(), y + marginTop, st.width(), st.styles());
                x += st.width();
            }
            y += text.height();
        }
        for (Caret c : carets.carets()) {
            Point p = c.pointFlush();
            view.locationOn(p.row(), p.col()).ifPresent(loc -> {
                draw.caret(loc.x() + marginLeft - scroll.xVal(), loc.y() + marginTop);
                if (c.hasFlush()) {
                    view.locationOn(c.point().row(), c.point().col()).ifPresent(org ->
                            draw.underline(org.x() + marginLeft - scroll.xVal(), org.y() + marginTop,
                                           loc.x() + marginLeft - scroll.xVal(), loc.y() + marginTop));
                }
            });
        }
        drawLeftGarter(draw);

    }

    private void drawLeftGarter(Draw draw) {
        List<Text> lineNumbers = view.lineNumbers();
        double nw = lineNumbers.stream().mapToDouble(Text::width).max().orElse(0);
        if (nw + 16 * 2 > marginLeft) {
            double newMarginLeft = nw + 8 * 2;
            setSize(view.width() + marginLeft - newMarginLeft, view.height());
            draw(draw);
            return;
        }
        draw.rect(0, 0, marginLeft - 5, view.height() + marginTop);
        double y = 0;
        for (Text num : lineNumbers) {
            String colorString = carets.points().stream().anyMatch(p -> p.row() == num.row())
                    ? Theme.dark.fgColor()
                    : Theme.dark.fgColor() + "66";
            draw.text(num.value(), marginLeft - 16 - num.width(), y + marginTop, num.width(),
                    List.of(new Style.TextColor(colorString)));
            y += num.height();
        }
    }

    @Override
    public void setSize(double width, double height) {
        view.setSize(width - marginLeft, height - marginTop);
    }

    @Override
    public void scrollNext(int delta) {
        view.scrollNext(delta);
    }

    @Override
    public void scrollPrev(int delta) {
        view.scrollPrev(delta);
    }

    @Override
    public void scrollAt(int line) {
        view.scrollAt(line);
    }

    @Override
    public void scrollX(double x) {
        view.scrollX(x);
    }

    @Override
    public void scrollToCaret() {
        Caret caret = carets.getFirst();
        int line = view.rowToLine(caret.row(), caret.col());
        if (line - view.topLine() < 0) {
            view.scrollAt(line);
        } else if (line - (view.topLine() + view.lineSizeOnView() - 3) > 0) {
            view.scrollAt(line - view.lineSizeOnView() + 3);
        }
        // TODO scroll x
    }

    @Override
    public void moveCaretRight(boolean withSelect) {
        for (Caret c : carets.carets()) {
            c.markIf(withSelect);
            var text = view.textAt(c.row());
            if (text == null) continue;
            int next = text.indexRight(c.col());
            if (next <= 0) {
                c.at(c.row() + 1, 0);
            } else {
                c.at(c.row(), next);
            }
        }
    }

    @Override
    public void moveCaretLeft(boolean withSelect) {
        for (Caret c : carets.carets()) {
            c.markIf(withSelect);
            if (c.isZero()) continue;
            if (c.col() == 0) {
                var text = view.textAt(c.row() - 1);
                c.at(c.row() - 1, text.textLength());
            } else {
                var text = view.textAt(c.row());
                int next = text.indexLeft(c.col());
                c.at(c.row(), next);
            }
        }
    }

    @Override
    public void moveCaretDown(boolean withSelect) {
        for (Caret c : carets.carets()) {
            c.markIf(withSelect);
            int line = view.rowToLine(c.row(), c.col());
            if (line == view.lineSize()) continue;
            double x = (c.vPos() < 0)
                    ? view.colToXOnLayout(line, c.col())
                    : c.vPos();
            line++;
            c.at(view.lineToRow(line), view.xToCol(line, x), x);
        }
    }

    @Override
    public void moveCaretUp(boolean withSelect) {
        for (Caret c : carets.carets()) {
            c.markIf(withSelect);
            int line = view.rowToLine(c.row(), c.col());
            if (line == 0) continue;
            double x = (c.vPos() < 0)
                    ? view.colToXOnLayout(line, c.col())
                    : c.vPos();
            line--;
            c.at(view.lineToRow(line), view.xToCol(line, x), x);
        }
    }

    @Override
    public void moveCaretHome(boolean withSelect) {
        for (Caret c : carets.carets()) {
            c.markIf(withSelect);
            int line = view.rowToLine(c.row(), c.col());
            c.at(c.row(), view.homeColOnRow(line));
        }
    }

    @Override
    public void moveCaretEnd(boolean withSelect) {
        for (Caret c : carets.carets()) {
            c.markIf(withSelect);
            int line = view.rowToLine(c.row(), c.col());
            c.at(c.row(), view.endColOnRow(line));
        }
    }

    @Override
    public void moveCaretPageUp(boolean withSelect) {
        int n = view.lineSizeOnView() - 1;
        scrollPrev(n);
        if (withSelect && carets.size() > 1) carets.unique();
        if (carets.size() == 1) {
            Caret c = carets.getFirst();
            c.markIf(withSelect);
            int line = view.rowToLine(c.row(), c.col());
            double x = view.colToXOnLayout(line, c.col());
            c.at(view.lineToRow(line - n), view.xToCol(line, x));
        }
    }

    @Override
    public void moveCaretPageDown(boolean withSelect) {
        int n = view.lineSizeOnView() - 1;
        scrollNext(n);
        if (withSelect && carets.size() > 1) carets.unique();
        if (carets.size() == 1) {
            Caret c = carets.getFirst();
            c.markIf(withSelect);
            int line = view.rowToLine(c.row(), c.col());
            double x = view.colToXOnLayout(line, c.col());
            c.at(view.lineToRow(line + n), view.xToCol(line, x));
        }
    }

    @Override
    public void click(double x, double y, boolean withSelect) {
        Caret c = carets.unique();
        if (c.isFloating()) {
            c.clearFloat();
            return;
        }
        c.clearMark();
        int line = view.yToLineOnView(y - marginTop);
        c.at(view.lineToRow(line), view.xToCol(line, x - marginLeft));
    }

    @Override
    public void clickDouble(double x, double y) {
        int line = view.yToLineOnView(y - marginTop);
        var text = view.text(line);
        double xp = 0;
        for (var word : text.words()) {
            if (xp + word.width() > x - marginLeft) {
                Caret c = carets.getFirst();
                int row = view.lineToRow(line);
                c.markTo(row, view.xToCol(line, xp),
                        row, view.xToCol(line, xp + word.width()));
                break;
            }
            xp += word.width();
        }
    }

    @Override
    public void clickTriple(double x, double y) {
        int line = view.yToLineOnView(y - marginTop);
        int row = view.lineToRow(line);
        Caret c = carets.unique();
        c.markTo(row, view.xToCol(line, 0), row, view.xToCol(line, Double.MAX_VALUE));
    }

    @Override
    public void moveDragged(double x, double y) {
        int line = view.yToLineOnView(y - marginTop);
        int row = view.lineToRow(line);
        int col = view.xToCol(line, x - marginLeft);
        Caret caret = carets.getFirst();
        caret.floatAt(row, col);
        caret.markIf(true);
    }

    @Override
    public void input(String text) {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            if (caret.isMarked()) {
                selectionReplace(caret, text);
            } else {
                var pos = content.insert(caret.point(), text);
                view.refreshBuffer(caret.row(), pos.row() + 1);
                caret.at(pos);
            }
        } else {
            // TODO
        }
    }

    @Override
    public void delete() {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            if (caret.isMarked()) {
                selectionReplace(caret, "");
            } else {
                var del = content.delete(caret.point());
                view.refreshBuffer(caret.row(), caret.row() + 1);
            }
        } else {
            // TODO
        }
    }

    @Override
    public void backspace() {
        if (carets.size() == 1) {
            Caret caret = carets.getFirst();
            if (caret.isMarked()) {
                selectionReplace(caret, "");
            } else {
                var pos = content.backspace(caret.point());
                view.refreshBuffer(pos.row(), caret.row() + 1);
                caret.at(pos);
            }
        } else {
            // TODO
        }
    }

    private Point selectionReplace(Caret caret, String text) {
        assert caret.isMarked();
        var range = caret.markedRange();
        var pos = content.replace(range.start(), range.end(), text);
        view.refreshBuffer(range.start().row(), range.end().row() + 1);
        caret.clearMark();
        caret.at(pos);
        return pos;
    }

    @Override
    public void undo() {
        carets.at(content.undo());
    }

    @Override
    public void redo() {
        carets.at(content.redo());
    }

    @Override
    public void pasteFromClipboard() {
        var clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        var text = clipboard.hasString() ? clipboard.getString() : "";
        if (text.isEmpty()) return;
        input(text);
    }

    @Override
    public void copyToClipboard() {
        String copy = carets.marked().stream()
                .map(range -> content.getText(range.min(), range.max()))
                .collect(Collectors.joining(System.lineSeparator()));
        if (copy.isEmpty()) return;
        javafx.scene.input.Clipboard.getSystemClipboard()
                .setContent(Map.of(DataFormat.PLAIN_TEXT, copy));
    }

    @Override
    public void cutToClipboard() {
        copyToClipboard();
        content.delete(carets.marked());
    }

    @Override
    public boolean isModified() {
        return content.isModified();
    }

    @Override
    public Optional<Path> path() {
        return content.path();
    }
    @Override
    public void save(Path path) {
        content.save(path);
    }

    @Override
    public void escape() {
        carets.unique().clearMark();
    }

    @Override
    public Optional<Loc> imeOn() {
        Caret caret = carets.getFirst();
        caret.flushAt(caret.point());
        return view.locationOn(caret.row(), caret.col())
                .map(top -> new Loc(top.x() + marginLeft, top.y() + marginTop + view.lineHeight() + 5));
    }

    @Override
    public void imeOff() {
        content.clearFlush();
        carets.getFirst().clearFlush();
    }

    @Override
    public boolean isImeOn() {
        return carets.getFirst().hasFlush();
    }

    @Override
    public void inputImeComposed(String text) {
        Caret caret = carets.getFirst();
        content.clearFlush();
        var pos = content.insertFlush(caret.point(), text);
        view.refreshBuffer(caret.row(), pos.row() + 1);
        caret.flushAt(pos);
    }

}
