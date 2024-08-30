package com.mammb.code.piecetable.examples;

import com.mammb.code.piecetable.examples.Text.RowDecorator;
import com.mammb.code.piecetable.examples.Style.*;
import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.TextEdit;
import com.mammb.code.piecetable.TextEdit.Pos;
import javafx.scene.input.DataFormat;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ScreenText {
    int MARGIN_TOP = 5, MARGIN_LEFT = 5;

    void draw(Draw draw);
    void setSize(double width, double height);
    int getScrollableMaxLine();
    int getScrolledLineValue();
    double getScrollableMaxX();
    double getScrolledXValue();
    void scrollX(double x);
    void scrollNext(int delta);
    void scrollPrev(int delta);
    void scrollAt(int n);
    void moveCaretRight();
    void moveCaretSelectRight();
    void moveCaretLeft();
    void moveCaretSelectLeft();
    void moveCaretDown();
    void moveCaretSelectDown();
    void moveCaretUp();
    void moveCaretSelectUp();
    void moveCaretHome();
    void moveCaretEnd();
    void moveCaretSelectHome();
    void moveCaretSelectEnd();
    void moveCaretPageUp();
    void moveCaretPageDown();
    void moveCaretSelectPageUp();
    void moveCaretSelectPageDown();
    void input(String text);
    void delete();
    void backspace();
    void undo();
    void redo();
    void click(double x, double y);
    void clickDouble(double x, double y);
    void clickTriple(double x, double y);
    void moveDragged(double x, double y);
    void pasteFromClipboard();
    void copyToClipboard();
    void cutToClipboard();
    Path path();
    void save(Path path);
    int screenLineSize();

    static ScreenText of(Document doc, FontMetrics fm, Syntax syntax) {
        return new PlainScreenText(doc, fm, syntax);
    }

    abstract class AbstractScreenText implements ScreenText {
        protected double width = 0, height = 0;
        protected int screenLineSize = 0;
        protected final TextEdit ed;
        protected final FontMetrics fm;
        protected final RowDecorator rowDecorator;
        protected final List<Caret> carets = new ArrayList<>();

        public AbstractScreenText(TextEdit ed, FontMetrics fm, Syntax syntax) {
            this.ed = ed;
            this.fm = fm;
            this.rowDecorator = RowDecorator.of(syntax);
            this.carets.add(new Caret(0, 0));
        }

        @Override
        public void moveCaretRight() {
            carets.forEach(c -> { c.clearMark(); moveCaretRight(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectRight() {
            carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretRight(c); });
            scrollToCaret();
        }
        private void moveCaretRight(Caret caret) {
            caret.vPos = -1;
            Text row = textRowAt(caret.row);
            if (row.text().isEmpty()) return;
            caret.col += row.isHighSurrogate(caret.col) ? 2 : 1;
            if (caret.col > row.textLength()) {
                caret.col = 0;
                caret.row = Math.min(caret.row + 1, ed.rows());
            }
        }
        @Override
        public void moveCaretLeft() {
            carets.forEach(c -> { c.clearMark(); moveCaretLeft(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectLeft() {
            carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretLeft(c); });
            scrollToCaret();
        }
        private void moveCaretLeft(Caret caret) {
            caret.vPos = -1;
            if (caret.isZero()) return;

            if (caret.col > 0) {
                Text textRow = textRowAt(caret.row);
                caret.col -= textRow.isLowSurrogate(caret.col - 1) ? 2 : 1;
            } else {
                caret.row = Math.max(0, caret.row - 1);
                Text textRow = textRowAt(caret.row);
                caret.col = textRow.textLength();
            }
        }
        @Override
        public void moveCaretDown() {
            carets.forEach(c -> { c.clearMark(); moveCaretDown(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectDown() {
            carets.forEach(c -> { if (!c.hasMark()) c.mark();  moveCaretDown(c); });
            scrollToCaret();
        }
        protected abstract void moveCaretDown(Caret caret);
        @Override
        public void moveCaretUp() {
            carets.forEach(c -> { c.clearMark(); moveCaretUp(c); });
            scrollToCaret();
        }
        @Override
        public void moveCaretSelectUp() {
            carets.forEach(c -> {if (!c.hasMark()) c.mark();  moveCaretUp(c); });
            scrollToCaret();
        }
        protected abstract void moveCaretUp(Caret caret);
        @Override
        public void moveCaretHome() { carets.forEach(c -> { c.clearMark(); moveCaretHome(c); }); }
        @Override
        public void moveCaretSelectHome() { carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretHome(c); }); }
        private void moveCaretHome(Caret caret) { caret.vPos = -1; caret.col = 0; }
        @Override
        public void moveCaretEnd() { carets.forEach(c -> { c.clearMark(); moveCaretEnd(c); }); }
        @Override
        public void moveCaretSelectEnd() { carets.forEach(c -> { if (!c.hasMark()) c.mark(); moveCaretEnd(c); }); }
        private void moveCaretEnd(Caret caret) { caret.vPos = -1; caret.col = textRowAt(caret.row).textLength(); }

        @Override
        public void moveCaretPageUp() { carets.forEach(Caret::clearMark); moveCaretPageUp(carets.getFirst()); }
        @Override
        public void moveCaretPageDown() { carets.forEach(Caret::clearMark); moveCaretPageDown(carets.getFirst()); }
        @Override
        public void moveCaretSelectPageUp() {
            carets.stream().skip(1).forEach(Caret::clearMark);
            Caret c = carets.getFirst();
            if (!c.hasMark()) c.mark();
            moveCaretPageUp(c);
        }
        @Override
        public void moveCaretSelectPageDown() {
            carets.stream().skip(1).forEach(Caret::clearMark);
            Caret c = carets.getFirst();
            if (!c.hasMark()) c.mark();
            moveCaretPageDown(c);
        }
        private void moveCaretPageUp(Caret caret) {
            scrollToCaret();
            Loc loc = posToLocInParent(caret.row, caret.col);
            scrollPrev(screenLineSize - 1);
            click(loc.x(), loc.y());
        }
        private void moveCaretPageDown(Caret caret) {
            scrollToCaret();
            Loc loc = posToLocInParent(caret.row, caret.col);
            scrollNext(screenLineSize - 1);
            click(loc.x(), loc.y());
        }

        public void scrollToCaret() {
            Caret caret = carets.getFirst();
            Loc loc = posToLoc(caret.row, caret.col);
            if (loc.y() < 0) {
                scrollPrev((int) Math.ceil(Math.abs(loc.y()) / fm.getLineHeight()));
            } else if (loc.y() + fm.getLineHeight() * 2 > height) {
                scrollNext((int) Math.ceil((loc.y() + fm.getLineHeight() * 2 - height) / fm.getLineHeight()));
            }
        }

        @Override
        public void input(String text) {
            if (carets.size() == 1) {
                Caret caret = carets.getFirst();
                if (caret.hasMark()) {
                    selectionReplace(caret, text);
                } else {
                    var pos = ed.insert(caret.row, caret.col, text);
                    if (caret.row == pos.row()) {
                        refreshBufferAt(caret.row);
                    } else {
                        refreshBufferRange(caret.row);
                    }
                    caret.at(pos.row(), pos.col());
                }
            } else {
                Collections.sort(carets);
                var poss = ed.insert(carets.stream().map(c -> new TextEdit.Pos(c.row, c.col)).toList(), text);
                refreshBufferRange(poss.getFirst().row());
                for (int i = 0; i < poss.size(); i++) {
                    var pos = poss.get(i);
                    carets.get(i).at(pos.row(), pos.col());
                }
            }
            scrollToCaret();
        }

        @Override
        public void delete() {
            if (carets.size() == 1) {
                Caret caret = carets.getFirst();
                if (caret.hasMark()) {
                    selectionReplace(caret, "");
                } else {
                    var del = ed.delete(caret.row, caret.col);
                    if (!del.contains("\n")) {
                        refreshBufferAt(caret.row);
                    } else {
                        refreshBufferRange(caret.row);
                    }
                }
            } else {
                Collections.sort(carets);
                var poss = ed.delete(carets.stream().map(c -> new TextEdit.Pos(c.row, c.col)).toList());
                refreshBufferRange(poss.getFirst().row());
                for (int i = 0; i < poss.size(); i++) {
                    var pos = poss.get(i);
                    carets.get(i).at(pos.row(), pos.col());
                }
            }
            scrollToCaret();
        }

        @Override
        public void backspace() {
            if (carets.size() == 1) {
                Caret caret = carets.getFirst();
                if (caret.isZero()) return;
                if (caret.hasMark()) {
                    selectionReplace(caret, "");
                } else {
                    var pos = ed.backspace(caret.row, caret.col);
                    if (caret.row == pos.row()) {
                        refreshBufferAt(caret.row);
                    } else {
                        refreshBufferRange(pos.row());
                    }
                    caret.at(pos.row(), pos.col());
                }
            } else {
                Collections.sort(carets);
                var poss = ed.backspace(carets.stream().map(c -> new TextEdit.Pos(c.row, c.col)).toList());
                refreshBufferRange(poss.getFirst().row());
                for (int i = 0; i < poss.size(); i++) {
                    var pos = poss.get(i);
                    carets.get(i).at(pos.row(), pos.col());
                }
            }
            scrollToCaret();
        }

        private void selectionReplace(Caret caret, String text) {
            assert caret.hasMark();
            var pos = ed.replace(caret.row, caret.col, caret.markedRow, caret.markedCol, text);
            refreshBufferRange(caret.markedMin().row());
            caret.clearMark();
            caret.at(pos.row(), pos.col());
        }

        @Override
        public void undo() {
            var newCarets = ed.undo().stream().map(p-> new Caret(p.row(), p.col())).toList();
            if (!newCarets.isEmpty()) {
                carets.clear();
                carets.addAll(newCarets);
                scrollToCaret();
                refreshBufferRange(carets.getFirst().row);
            }
        }
        @Override
        public void redo() {
            var newCarets = ed.redo().stream().map(p-> new Caret(p.row(), p.col())).toList();
            if (!newCarets.isEmpty()) {
                carets.clear();
                carets.addAll(newCarets);
                scrollToCaret();
                refreshBufferRange(carets.getFirst().row);
            }
        }
        @Override
        public void clickDouble(double x, double y) { /* Not yet implemented. */ }
        @Override
        public void clickTriple(double x, double y) { /* Not yet implemented. */ }
        protected void refreshBufferAt(List<Caret> carets) {
            carets.stream().mapToInt(c -> c.row).distinct().forEach(this::refreshBufferAt);
        }
        protected abstract void refreshBufferAt(int row);
        protected abstract void refreshBufferRange(int fromRow);
        protected abstract void refreshBufferRange(int row, int nRow);
        protected Text textRowAt(int row) {
            return createRow(row);
        }
        protected Text createStyledRow(int row) {
            return rowDecorator.apply(createRow(row));
        }
        protected Text createRow(int row) {
            String text = ed.getText(row);
            return Text.of(row, text, Text.advances(text, fm), fm.getLineHeight());
        }
        protected abstract Loc posToLoc(int row, int col);
        protected abstract Loc posToLocInParent(int row, int col);
        protected abstract TextEdit.Pos locToPos(double x, double y);
        protected int screenLineSize(double h) {
            return (int) Math.ceil(Math.max(0, h - MARGIN_TOP) / fm.getLineHeight());
        }
        @Override
        public void pasteFromClipboard() {
            var clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            var text = clipboard.hasString() ? clipboard.getString() : "";
            if (text == null || text.isEmpty()) {
                return;
            }
            input(text);
        }
        @Override
        public void copyToClipboard() {
            String copy = carets.stream().sorted().filter(Caret::hasMark)
                .map(c -> ed.getText(c.markedMin(), c.markedMax()))
                .collect(Collectors.joining());
            javafx.scene.input.Clipboard.getSystemClipboard()
                .setContent(Map.of(DataFormat.PLAIN_TEXT, copy));
        }
        @Override
        public void cutToClipboard() {
            List<Caret> select = carets.stream().sorted().filter(Caret::hasMark).toList();
            List<String> texts = select.stream()
                .map(c -> String.join("", ed.getText(c.markedMin(), c.markedMax())))
                .toList();
            for (int i = 0; i < select.size(); i++) {
                Caret c = select.get(i);
                ed.delete(c.markedMin().row(), c.markedMin().col(), texts.get(i).length());
                // TODO transaction delete
            }
            javafx.scene.input.Clipboard.getSystemClipboard()
                .setContent(Map.of(DataFormat.PLAIN_TEXT, String.join("", texts)));
        }
        @Override
        public Path path() { return ed.path(); }
        @Override
        public void save(Path path) { ed.save(path); }
        @Override
        public int screenLineSize() { return screenLineSize; }
    }

    class PlainScreenText extends AbstractScreenText {
        private double maxRowWidth = 0;
        private double xShift = 0;
        private final List<Text> buffer = new ArrayList<>();

        public PlainScreenText(Document doc, FontMetrics fm, Syntax syntax) {
            super(TextEdit.of(doc), fm, syntax);
        }

        @Override
        public void draw(Draw draw) {
            draw.clear();
            if (buffer.isEmpty()) return;

            for (Caret caret : carets) {
                if (!caret.hasMark()) continue;
                Pos min = caret.markedMin();
                Pos max = caret.markedMax();
                Loc minLoc = posToLoc(min.row(), min.col());
                Loc maxLoc = posToLoc(max.row(), max.col());
                draw.fillSelection(
                    minLoc.x() + MARGIN_LEFT - xShift, minLoc.y() + MARGIN_TOP,
                    maxLoc.x() + MARGIN_LEFT - xShift, maxLoc.y() + MARGIN_TOP,
                    MARGIN_LEFT, width);
            }

            double y = 0;
            maxRowWidth = 0;
            for (Text row : buffer) {
                double x = 0;
                for (StyledText st : row.styledTexts()) {
                    draw.text(st.text(), x + MARGIN_LEFT - xShift, y + MARGIN_TOP, st.width(), st.styles());
                    x += st.width();
                }
                y += row.lineHeight();
                maxRowWidth = Math.max(maxRowWidth, row.width());
            }
            for (Caret caret : carets) {
                if (buffer.getFirst().row() <= caret.row && caret.row <= buffer.getLast().row()) {
                    double x = colToX(caret.row, caret.col);
                    caret.vPos = (caret.vPos < 0) ? x : caret.vPos;
                    draw.caret(
                        Math.min(x, caret.vPos) + MARGIN_LEFT - xShift,
                        rowToY(caret.row) + MARGIN_TOP);
                }
            }
        }

        @Override
        public void setSize(double width, double height) {
            if (width <= 0 || height <= 0) return;
            int newScreenLineSize = screenLineSize(height);
            if (this.height > height) {
                // shrink height
                int fromIndex = newScreenLineSize + 1;
                if (fromIndex < buffer.size() - 1) {
                    buffer.subList(fromIndex, buffer.size()).clear();
                }
            } else if (this.height < height) {
                // grow height
                int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
                for (int i = buffer.size(); i <= newScreenLineSize && i < ed.rows(); i++) {
                    buffer.add(createStyledRow(top + i));
                }
            }
            this.width = width;
            this.height = height;
            this.screenLineSize = newScreenLineSize;
        }

        @Override
        public int getScrollableMaxLine() { return (int) Math.max(0, ed.rows() - screenLineSize * 0.6); }
        @Override
        public int getScrolledLineValue() { return buffer.isEmpty() ? 0 : buffer.getFirst().row(); }
        @Override
        public double getScrollableMaxX() {
            return  (maxRowWidth > width) ? Math.max(0, maxRowWidth + MARGIN_LEFT - width * 0.6) : 0;
        }
        @Override
        public double getScrolledXValue() { return xShift; }
        @Override
        public void scrollX(double x) { xShift = x; }

        @Override
        public void scrollNext(int delta) {
            assert delta > 0;

            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            int maxTop = getScrollableMaxLine();
            if (top + delta >= maxTop) {
                delta = maxTop - top;
            }

            if (delta == 0) return;
            if (delta >= screenLineSize) {
                scrollAt(top + delta);
                return;
            }

            int next = buffer.isEmpty() ? 0 : buffer.getLast().row() + 1;
            buffer.subList(0, Math.min(delta, buffer.size())).clear();
            for (int i = next; i < (next + delta) && i < ed.rows(); i++) {
                buffer.add(createStyledRow(i));
            }
        }

        @Override
        public void scrollPrev(int delta) {
            assert delta > 0;
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            delta = Math.clamp(delta, 0, top);

            if (delta == 0) return;
            if (delta >= screenLineSize) {
                scrollAt(top - delta);
                return;
            }

            if (buffer.size() >= screenLineSize) {
                buffer.subList(Math.max(0, buffer.size() - delta), buffer.size()).clear();
            }
            for (int i = 1; i <= delta; i++) {
                buffer.addFirst(createStyledRow(top - i));
            }
        }

        @Override
        public void scrollAt(int row) {
            row = Math.clamp(row, 0, getScrollableMaxLine());
            buffer.clear();
            for (int i = row; i < ed.rows(); i++) {
                buffer.add(createStyledRow(i));
                if (buffer.size() >= screenLineSize) break;
            }
        }

        @Override
        protected void moveCaretDown(Caret caret) {
            if (caret.row == ed.rows()) return;
            caret.vPos = (caret.vPos < 0) ? colToX(caret.row, caret.col) : caret.vPos;
            caret.row++;
            caret.col = xToCol(caret.row, caret.vPos);
        }

        @Override
        protected void moveCaretUp(Caret caret) {
            if (caret.row == 0) return;
            caret.vPos = (caret.vPos < 0) ? colToX(caret.row, caret.col) : caret.vPos;
            caret.row--;
            caret.col = xToCol(caret.row, caret.vPos);
        }

        @Override
        public void click(double x, double y) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            TextEdit.Pos pos = locToPos(x + xShift, y + top * fm.getLineHeight());
            Caret caret = carets.getFirst();
            if (!caret.hasMarkOpen()) caret.clearMark();
            caret.at(pos.row(), pos.col());
        }
        @Override
        public void moveDragged(double x, double y) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            Pos pos = locToPos(x + xShift, y + top * fm.getLineHeight());
            Caret caret = carets.getFirst();
            caret.atOpen(pos.row(), pos.col());
            if (!caret.hasMark()) caret.mark();
        }

        @Override
        protected void refreshBufferRange(int fromRow) {
            int bufferIndex = bufferIndexOf(fromRow);
            for (int i = bufferIndex; i <= screenLineSize && i < ed.rows(); i++) {
                if (i >= buffer.size()) {
                    buffer.add(createStyledRow(fromRow++));
                } else {
                    buffer.set(i, createStyledRow(fromRow++));
                }
            }
        }
        @Override
        protected void refreshBufferAt(int row) {
            refreshBufferRange(row, 1);
        }
        @Override
        protected void refreshBufferRange(int row, int nRow) {
            assert row >= 0 && nRow > 0;
            for (int i = row; i < row + nRow; i++) {
                int bufferIndex = bufferIndexOf(i);
                if (bufferIndex >= 0) buffer.set(bufferIndex, createStyledRow(row));
            }
        }
        private int bufferIndexOf(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            int index = row - top;
            return (0 <= index && index < buffer.size()) ? index : -1;
        }

        @Override
        protected Loc posToLoc(int row, int col) {
            return new Loc(colToX(row, col), rowToY(row));
        }
        @Override
        protected Loc posToLocInParent(int row, int col) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            double y = (row - top) * fm.getLineHeight() + MARGIN_TOP;
            return new Loc(colToX(row, col) - xShift + MARGIN_LEFT, y);
        }
        @Override
        protected TextEdit.Pos locToPos(double x, double y) {
            int row = yToRow(y);
            int col = xToCol(row, x);
            return new TextEdit.Pos(row, col);
        }
        private double rowToY(int row) {
            int top = buffer.isEmpty() ? 0 : buffer.getFirst().row();
            return (row - top) * fm.getLineHeight();
        }
        private double colToX(int row, int col) {
            double[] advances = textRowAt(row).advances();
            double x = 0;
            for (int i = 0; i < advances.length && i < col; i++) {
                x += advances[i];
            }
            return x;
        }
        private int xToCol(int row, double x) {
            if (x <= 0) return 0;
            Text textRow = textRowAt(row);
            double[] advances = textRow.advances();
            for (int i = 0; i < advances.length; i++) {
                x -= advances[i];
                if (x < 0) return i;
            }
            return textRow.textLength();
        }
        private int yToRow(double y) {
            return Math.clamp((int) (Math.max(0, y - MARGIN_TOP) / fm.getLineHeight()), 0, ed.rows() - 1);
        }

        @Override
        protected Text textRowAt(int row) {
            if (buffer.isEmpty()) {
                return createRow(row);
            }
            var top = buffer.getFirst();
            if (top.row() == row) return top;
            if (top.row() <= row && row < top.row() + buffer.size()) {
                return buffer.get(row - top.row());
            } else {
                return createRow(row);
            }
        }
    }

    record Loc(double x, double y) { }

    class Caret implements Comparable<Caret> {
        int row = 0, col = 0;
        double vPos = 0; // not contains margin
        int markedRow = -1, markedCol = -1;
        boolean markOpened;
        Caret(int row, int col) { this.row = row; this.col = col; vPos = -1; }
        public void at(int row, int col) { this.row = row; this.col = col; vPos = -1; markOpened = false; }
        public void atOpen(int row, int col) { this.row = row; this.col = col; vPos = -1; markOpened = true; }
        public void mark(int row, int col) { markedRow = row; markedCol = col; }
        public void mark() { markedRow = row; markedCol = col; }
        public void clearMark() { markedRow = -1; markedCol = -1; markOpened = false; }
        public boolean isZero() { return row == 0 && col == 0; }
        public boolean hasMark() { return markedRow >= 0 && markedCol >= 0 && !(row == markedRow && col == markedCol); }
        public boolean hasMarkOpen() { return hasMark() && markOpened; }
        public boolean isMarkForward() { return hasMark() && ((row == markedRow && col > markedCol) || (row > markedRow)); }
        public boolean isMarkBackward() { return hasMark() && !isMarkForward(); }
        public Pos markedMin() { return isMarkForward() ? new Pos(markedRow, markedCol) : new Pos(row, col); }
        public Pos markedMax() { return isMarkBackward() ? new Pos(markedRow, markedCol) : new Pos(row, col);}
        @Override public int compareTo(Caret that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }

}
