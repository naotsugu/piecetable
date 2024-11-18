/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.piecetable.edit;

import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.Document.RowEnding;
import com.mammb.code.piecetable.Found;
import com.mammb.code.piecetable.TextEdit;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mammb.code.piecetable.edit.Texts.*;

/**
 * The {@link TextEdit} implementation.
 * @author Naotsugu Kobayashi
 */
public class TextEditImpl implements TextEdit {

    /** The edit queue. */
    private final Deque<Edit> deque = new ArrayDeque<>();

    /** The undo queue. */
    private final Deque<Edit> undo = new ArrayDeque<>();

    /** The redo queue. */
    private final Deque<Edit> redo = new ArrayDeque<>();

    /** The dry run buffer. */
    private final Map<Integer, String> dryBuffer = new HashMap<>();

    /** The document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public TextEditImpl(Document doc) {
        this.doc = doc;
    }

    // -- Insert --------------------------------------------------------------

    @Override
    public Pos insert(int row, int col, String text) {
        if (text.isEmpty()) return new Pos(row, col);
        var edit = insertEdit(row, col, text, System.currentTimeMillis());
        push(edit);
        return edit.to();
    }

    @Override
    public List<Pos> insert(List<Pos> posList, String text) {
        long occurredOn = System.currentTimeMillis();
        List<Edit.ConcreteEdit> edits = new ArrayList<>();

        List<String> lines = splitRowBreak(text);

        Edit.ConcreteEdit prevEdit = null;
        Pos prevPos = null;
        int piledRow = 0;
        int piledCol = 0;
        for (Pos pos : posList.stream().sorted().distinct().toList()) {
            int row = pos.row();
            int col = pos.col();

            row += piledRow;
            if (prevEdit != null && prevEdit.from().row() != prevEdit.to().row() && prevEdit.to().row() == row) {
                piledCol -= prevPos.col();
            } else if (prevEdit != null && prevEdit.to().row() != row) {
                // different lines
                piledCol = 0;
            }
            col += piledCol;
            Edit.Ins edit = insertEdit(row, col, text, occurredOn);
            edits.add(edit);

            prevEdit = edit;
            prevPos = pos;
            piledRow += (lines.size() - 1);
            piledCol = (lines.size() > 1)
                ? lines.getLast().length()
                : piledCol + lines.getFirst().length();
        }

        Edit edit = new Edit.Cmp(edits, occurredOn);
        push(edit);
        return edits.stream().map(e -> new Pos(e.to().row(), e.to().col())).toList();
    }
    // -- Delete --------------------------------------------------------------

    @Override
    public String delete(int row, int col) {
        return deleteChar(row, col, 1);
    }

    @Override
    public String delete(int row, int col, int len) {
        var del = join(textRightByte(row, col, len));
        Edit.Del edit = deleteEdit(row, col, del, System.currentTimeMillis());
        push(edit);
        return edit.text();
    }

    @Override
    public List<Pos> delete(List<Pos> posList) {
        return deleteChar(posList, 1);
    }

    private String deleteChar(int row, int col, int chCount) {
        var del = join(textRight(row, col, chCount));
        Edit.Del edit = deleteEdit(row, col, del, System.currentTimeMillis());
        push(edit);
        return edit.text();
    }

    //
    //  | a | b | c | d | $ |
    //                  ^****
    //  | e | f | g | h | i | j | k | l | $ |
    //  ****    ^*******    ^*******
    //  | m | n | o | $ |
    //      ^*******
    //  ------------------------------
    //  delete [$ e][g h][j k][n o]
    //  ------------------------------
    //
    //  | a | b | c | d | f | i | l | $ |
    //                  ^   ^   ^
    //  | m | $ |
    //      ^
    List<Pos> deleteChar(List<Pos> posList, int chCount) {
        long occurredOn = System.currentTimeMillis();
        List<Edit.ConcreteEdit> edits = new ArrayList<>();

        Edit.ConcreteEdit prevEdit = null;
        int piledRow = 0;
        int piledCol = 0;
        for (Pos pos : posList.stream().sorted().distinct().toList()) {

            int row = pos.row();
            int col = pos.col();
            List<String> lines = textRight(row, col, chCount);

            row -= piledRow;
            if (prevEdit != null && countRowBreak(prevEdit.text()) > 0 && prevEdit.from().row() == row) {
                // merged
                piledCol = -(prevEdit.to().col() - splitRowBreak(prevEdit.text()).getLast().length());
            } else if (prevEdit != null && prevEdit.from().row() != row) {
                // different lines
                piledCol = 0;
            }
            col -= piledCol;

            Edit.Del edit = deleteEdit(row, col, join(lines), occurredOn);
            edits.add(edit);

            prevEdit = edit;
            piledRow += countRowBreak(edit.text());
            piledCol += lines.getLast().length();
        }

        Edit edit = new Edit.Cmp(edits, occurredOn);
        push(edit);
        return edits.stream().map(e -> new Pos(e.to().row(), e.to().col())).toList();
    }

    // -- Backspace -----------------------------------------------------------

    @Override
    public Pos backspace(int row, int col) {
        return backspaceChar(row, col, 1);
    }

    @Override
    public Pos backspace(int row, int col, int len) {
        var del = join(textLeftByte(row, col, len));
        Edit.Del edit = backspaceEdit(row, col, del, System.currentTimeMillis());
        push(edit);
        return edit.to();
    }

    @Override
    public List<Pos> backspace(List<Pos> posList) {
        return backspaceChar(posList, 1);
    }

    Pos backspaceChar(int row, int col, int chCount) {
        var del = join(textLeft(row, col, chCount));
        Edit.Del edit = backspaceEdit(row, col, del, System.currentTimeMillis());
        push(edit);
        return edit.to();
    }

    List<Pos> backspaceChar(List<Pos> posList, int chCount) {

        long occurredOn = System.currentTimeMillis();
        List<Edit.ConcreteEdit> edits = new ArrayList<>();
        for (Pos pos : posList.stream().sorted(Comparator.reverseOrder()).distinct().toList()) {
            var lines = textLeft(pos.row(), pos.col(), chCount);
            Edit.Del del = backspaceEdit(pos.row(), pos.col(), join(lines), occurredOn);
            edits.add(del);
        }
        Edit edit = new Edit.Cmp(edits, occurredOn);
        push(edit);

        Edit.ConcreteEdit prevEdit = edits.getLast();
        int piledRow = 0;
        int piledCol = 0;
        List<Pos> ret = new ArrayList<>();
        for (Edit.ConcreteEdit e : edits.stream()
            .sorted(Comparator.comparing(Edit.ConcreteEdit::to)).toList()) {
            int row = e.to().row();
            int col = e.to().col();
            if (prevEdit.to().row() != prevEdit.from().row() && prevEdit.from().row() == row) {
                // merged
                piledCol -= prevEdit.to().col();
            } else if (row != prevEdit.from().row()) {
                // different lines
                piledCol = 0;
            }
            row -= piledRow;
            col -= piledCol;
            ret.add(new Pos(row, col));
            prevEdit = e;
            piledRow += countRowBreak(e.text());
            piledCol += splitRowBreak(e.text()).getLast().length();
        }
        return ret;
    }

    // -- Replace -------------------------------------------------------------

    @Override
    public Pos replace(int row, int col, int len, String text) {
        if (len == 0) {
            return insert(row, col, text);
        }
        long occurredOn = System.currentTimeMillis();
        Edit e;
        Pos pos;
        if (len > 0) {
            var delText = join(textRightByte(row, col, len));
            Edit.Del del = deleteEdit(row, col, delText, occurredOn);
            Edit.Ins ins = insertEdit(row, col, text, occurredOn);
            e = new Edit.Cmp(List.of(del, ins), occurredOn);
            pos = ins.to();
        } else {
            var delText = join(textLeftByte(row, col, -len));
            Edit.Del bs  = backspaceEdit(row, col, delText, occurredOn);
            Edit.Ins ins = insertEdit(bs.to().row(), bs.to().col(), text, occurredOn);
            e = new Edit.Cmp(List.of(bs, ins), occurredOn);
            pos = ins.to();
        }
        push(e);
        return pos;
    }

    // -- Undo / Redo ---------------------------------------------------------

    @Override
    public List<Pos> undo() {
        flush();
        final Optional<Edit> undo = undoEdit();
        if (undo.isEmpty()) {
            return List.of();
        }
        return switch (undo.get()) {
            case Edit.ConcreteEdit e -> List.of(e.to());
            case Edit.Cmp e -> List.of(e.edits().getLast().to());
        };
    }

    @Override
    public List<Pos> redo() {
        flush();
        final Optional<Edit> redo = redoEdit();
        if (redo.isEmpty()) {
            return List.of();
        }
        return switch (redo.get()) {
            case Edit.ConcreteEdit e -> List.of(e.to());
            case Edit.Cmp e -> List.of(e.edits().getLast().to());
        };
    }

    @Override
    public boolean hasUndoRecord() {
        return !undo.isEmpty();
    }

    @Override
    public String getText(int row) {
        if (!deque.isEmpty() && dryBuffer.isEmpty()) {
            dryApply();
        }
        if (dryBuffer.containsKey(row)) {
            return dryBuffer.get(row);
        }
        return doc.getText(row).toString();
    }

    @Override
    public String getText(int fromRow, int endRowExclusive) {
        return IntStream.range(fromRow, endRowExclusive)
            .mapToObj(this::getText)
            .collect(Collectors.joining());
    }

    @Override
    public String getText(Pos start, Pos end) {
        if (end.compareTo(start) < 0) {
                Pos temp = start;
                start = end;
                end = temp;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start.row(); i <= end.row(); i++) {
                String row = getText(i);
                row = (i == end.row()) ? row.substring(0, end.col()) : row;
                row = (i == start.row()) ? row.substring(start.col()) : row;
                sb.append(row);
        }
        return sb.toString();
    }

    @Override
    public List<String> getTexts(Pos start, Pos end) {
        if (end.compareTo(start) < 0) {
            Pos temp = start;
            start = end;
            end = temp;
        }
        List<String> list = new ArrayList<>();
        for (int i = start.row(); i <= end.row(); i++) {
            String row = getText(i);
            row = (i == end.row()) ? row.substring(0, end.col()) : row;
            row = (i == start.row()) ? row.substring(start.col()) : row;
            list.add(row);
        }
        return list;
    }

    @Override
    public void flush() {
        while (!deque.isEmpty()) {
            Edit edit = deque.pop();
            apply(edit);
            undo.push(edit.flip());
            redo.clear();
        }
        dryBuffer.clear();
    }

    @Override
    public void clear() {
        flush();
        undo.clear();
        redo.clear();
    }

    @Override
    public List<Found> findAll(String text) {
        flush();
        return doc.findAll(text);
    }

    @Override
    public Optional<Found> findNext(String text, int row, int col) {
        flush();
        return doc.findNext(text, row, col);
    }

    @Override
    public int rows() {
        return doc.rows();
    }

    @Override
    public long rawSize() {
        flush();
        return doc.rawSize();
    }

    @Override
    public Charset charset() {
        return doc.charset();
    }

    @Override
    public RowEnding rowEnding() {
        return doc.rowEnding();
    }

    @Override
    public byte[] bom() {
        return doc.bom();
    }

    @Override
    public Path path() {
        return doc.path();
    }

    @Override
    public void save(Path path) {
        flush();
        doc.save(path);
    }

    Edit.Ins insertEdit(int row, int col, String text, long occurredOn) {
        int index = text.lastIndexOf('\n');
        if (index < 0) {
            return new Edit.Ins(
                new Pos(row, col),
                new Pos(row, col + text.length()),
                text,
                occurredOn);
        } else {
            int nRow = row + (int) text.chars().mapToLong(c -> c == '\n' ? 1 : 0).sum();
            int nCol = text.substring(index + 1).length();
            return new Edit.Ins(
                new Pos(row, col),
                new Pos(nRow, nCol),
                text,
                occurredOn);
        }
    }

    Edit.Del deleteEdit(int row, int col, String text, long occurredOn) {
        return new Edit.Del(new Pos(row, col), text, occurredOn);
    }

    Edit.Del backspaceEdit(int row, int col, String text, long occurredOn) {
        int newRow = row - countRowBreak(text);
        int newCol = (row == newRow)
            ? col - text.length()
            : getText(newRow).length() - (text.indexOf('\n') + 1);
        return new Edit.Del(
            new Pos(row, col),
            new Pos(newRow, newCol),
            text,
            occurredOn);
    }

    /**
     * Push the edit.
     * @param edit the edit
     */
    private void push(final Edit edit) {
        Edit last = deque.peekLast();
        if (last != null) {
            Optional<Edit> merged = last.merge(edit);
            if (merged.isPresent()) {
                deque.removeLast();
                flush();
                deque.push(merged.get());
                return;
            }
        }
        flush();
        deque.push(edit);

        switch (edit) {
            // Multi-row edit is not supported at this time.
            case Edit.ConcreteEdit e when e.text().contains("\n") -> flush();
            case Edit.Cmp e when e.edits().stream().map(Edit.ConcreteEdit::text).anyMatch(s -> s.contains("\n")) -> flush();
            default -> { }
        }
    }

    private Optional<Edit> undoEdit() {
        flush();
        if (undo.isEmpty()) return Optional.empty();
        Edit edit = undo.pop();
        apply(edit);
        redo.push(edit.flip());
        return Optional.of(edit);
    }

    private Optional<Edit> redoEdit() {
        flush();
        if (redo.isEmpty()) return Optional.empty();
        Edit edit = redo.pop();
        apply(edit);
        undo.push(edit.flip());
        return Optional.of(edit);
    }

    private void apply(Edit edit) {
        switch (edit) {
            case Edit.Ins e -> doc.insert(e.min().row(), e.min().col(), e.text());
            case Edit.Del e -> doc.delete(e.min().row(), e.min().col(), e.text());
            case Edit.Cmp e -> e.edits().forEach(this::apply);
        }
    }

    private void dryApply() {
        dryBuffer.clear();
        deque.forEach(this::dryApply);
    }

    private void dryApply(Edit edit) {
        switch (edit) {
            case Edit.Ins e -> {
                String row = dryBuffer.get(e.from().row());
                if (row == null) {
                    row = doc.getText(e.from().row()).toString();
                }
                row = row.substring(0, e.min().col()) + e.text() + row.substring(e.min().col());
                dryBuffer.put(e.from().row(), row);
            }
            case Edit.Del e -> {
                String row = dryBuffer.get(e.from().row());
                if (row == null) {
                    row = doc.getText(e.from().row()).toString();
                }
                row = row.substring(0, e.min().col()) + row.substring(e.min().col() + e.text().length());
                dryBuffer.put(e.from().row(), row);
            }
            case Edit.Cmp e -> e.edits().forEach(this::dryApply);
        }
    }

    /**
     * <pre>
     *         textRight(0, 1, 7)
     *  row                             ->    list
     *   0   | * | * | * | \r | \n |            0  | * | * | \r | \n |
     *             1   2   3
     *   1   | h | l | * | * | * | * |          1  | h | l | * | * | * |
     *         4       5   6   7
     *
     *   h: high surrogate
     *   l: low surrogate
     * </pre>
     */
    List<String> textRight(int row, int col, int chLen) {
        List<String> ret = new ArrayList<>();
        for (int i = row; ; i++) { // i < doc.rows() : cannot get correct value if not flushed
            var text = getText(i).substring(col);
            if (text.isEmpty()) break;
            int len = Texts.chLength(text);
            if (chLen - len <= 0) {
                var left = Texts.left(text, chLen);
                ret.add(left);
                break;
            }
            ret.add(text);
            chLen -= len;
            col = 0;
        }
        if (!ret.isEmpty() && ret.getLast().endsWith("\n")) ret.add("");
        return ret;
    }

    /**
     * <pre>
     *         textLeft(1, 5, 7)
     *  row                             ->    list
     *   0   | * | * | * | \r | \n |            0  | * | * | \r | \n |
     *             7   6        5
     *   1   | h | l | * | * | * | * |          1  | h | l | * | * | * |
     *             4   3   2   1
     *
     *   h: high surrogate
     *   l: low surrogate
     * </pre>
     */
    List<String> textLeft(int row, int col, int chLen) {
        List<String> ret = new ArrayList<>();
        for (int i = row; i >= 0; i--) {
            var s = getText(i);
            var text = (i == row) ? s.substring(0, col) : s;
            int len = Texts.chLength(text);
            if (chLen - len <= 0) {
                ret.addFirst(Texts.right(text, chLen));
                break;
            }
            ret.addFirst(text);
            chLen -= len;
        }
        return ret;
    }


    List<String> textRightByte(int row, int col, int byteLen) {
        List<String> ret = new ArrayList<>();
        for (int i = row; ; i++) { // i < doc.rows() : cannot get correct value if not flushed
            var text = getText(i).substring(col);
            if (text.isEmpty()) break;
            int len = text.length();
            if (byteLen - len <= 0) {
                ret.add(text.substring(0, byteLen));
                break;
            }
            ret.add(text);
            byteLen -= len;
            col = 0;
        }
        if (!ret.isEmpty() && ret.getLast().endsWith("\n")) ret.add("");
        return ret;
    }

    List<String> textLeftByte(int row, int col, int byteLen) {
        List<String> ret = new ArrayList<>();
        if (col == 0) {
            ret.add("");
            row--;
        }
        for (int i = row; i >= 0; i--) {
            var s = getText(i);
            var text = (col > 0) ? s.substring(0, col) : s;
            int len = text.length();
            if (byteLen - len <= 0) {
                ret.addFirst(text.substring(len - byteLen));
                break;
            }
            ret.addFirst(text);
            byteLen -= len;
            col = 0;
        }
        return ret;
    }

    Document getDoc() { return doc; }
    Map<Integer, String> getDryBuffer() { return dryBuffer; }
    Deque<Edit> getDeque() { return deque; }
    Deque<Edit> getUndo() { return undo; }
    Deque<Edit> getRedo() { return redo; }
    void testPush(final Edit edit) { push(edit); }
    void testDryApply() { dryApply(); }
    void testDryApply(Edit edit) { dryApply(edit); }

    /**
     * Converts the relationship between positions into the distances.
     * @param poss the positions
     * @return the distances
     */
    int[] distances(List<Pos> poss) {
        int distance = 0;
        int index = 0;
        int[] ret = new int[poss.size()];
        Pos pos = poss.get(index);
        for (int i = poss.getFirst().row(); i <= poss.getLast().row(); i++) {
            String rowText = getText(i);
            while (pos.row() == i) {
                ret[index++] = distance + pos.col();
                if (index >= poss.size()) break;
                pos = poss.get(index);
            }
            distance += rowText.length();
        }
        return ret;
    }

    /**
     * Converts the distances into the positions.
     * @param row the base row
     * @param distances the distances
     * @return the positions
     */
    List<Pos> posList(int row, int[] distances) {
        List<Pos> poss = new ArrayList<>();
        int total = 0;
        int index = 0;
        for (int i = row; index < distances.length; i++) {
            String rowText = getText(i);
            if (rowText.isEmpty()) break;
            while (total + rowText.length() >= distances[index]) {
                poss.add(new Pos(i, distances[index] - total));
                index++;
                if (index >= distances.length) break;
            }
            total += rowText.length();
        }
        return poss;
    }


}
