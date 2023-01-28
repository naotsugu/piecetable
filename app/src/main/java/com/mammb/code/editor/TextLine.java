package com.mammb.code.editor;

import com.mammb.code.syntax.Highlighter;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.paint.Paint;
import javafx.scene.shape.PathElement;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.ERROR;

public class TextLine extends TextFlow {

    private static final System.Logger logger = System.getLogger(TextLine.class.getName());

    private final List<List<Text>> lines = new ArrayList<>();
    private final double textWidth = Utils.getTextWidth(Fonts.main, 1);
    private Highlighter highlighter;
    private Dirty dirty;

    private static final Text blankText = new Text("X");
    static { blankText.setFont(Fonts.main); blankText.setTextOrigin(VPos.TOP);}


    public TextLine() {
        highlighter = Highlighter.of("");
        setPadding(new Insets(4));
        setTabSize(4);
        setMaxWidth(Double.MAX_VALUE);
        addAll(0, 0, List.of(""));
    }


    public void init(String contentName) {
        highlighter = Highlighter.of(contentName);
    }


    public void addAll(int lineNumber, int index, Collection<? extends String> texts) {
        if (dirty != null && dirty.originLineNumber() != lineNumber - index) {
            logger.log(ERROR, "originLineNumber:{}, lineNumber{}, index{}", dirty.originLineNumber(), lineNumber, index);
        }
        int nodeIndex = asNodeIndex(index);
        List<List<Text>> adding = texts.stream().map(text -> asTexts(lineNumber, text)).toList();
        lines.addAll(index, adding);
        adding.forEach(this::fitPrefWidth);
        getChildren().addAll(nodeIndex, adding.stream().flatMap(Collection::stream).toList());
    }


    public void remove(int lineNumber, int index, int length) {
        if (dirty != null && dirty.originLineNumber() != lineNumber - index) {
            logger.log(ERROR, "originLineNumber:{}, lineNumber{}, index{}", dirty.originLineNumber(), lineNumber, index);
        }

        List<List<Text>> removingSublist = lines.subList(index, index + length);
        List<List<Text>> removingCopy = new ArrayList<>(removingSublist);
        removingSublist.clear();

        if (dirty != null) dirty.removeAll(removingCopy);
        List<Text> nodes = removingCopy.stream().flatMap(Collection::stream).toList();
        getChildren().removeAll(nodes);
    }


    public void markDirty(int lineNumber, int index, int length) {
        boolean requiredDirty = highlighter.invalidate(lineNumber, length);
        List<List<Text>> dirtyLines = new ArrayList<>(lines.subList(index, lines.size()));
        dirty = new Dirty(dirtyLines, requiredDirty, lineNumber, index);
    }


    public void cleanDirty() {

        final Dirty target = dirty;
        dirty = null;

        if (target.dirtyLines().isEmpty()) return;
        if (!target.invalidated() &&
            !highlighter.blockEdgeContains(target.lineNumber, 1)) return;

        for (List<Text> lineText : target.dirtyLines()) {
            int index = lines.indexOf(lineText);
            if (index > -1) {
                getChildren().removeAll(lines.get(index));
                List<Text> replacing = asTexts(target.originLineNumber() + index, lineString(index));
                lines.set(index, replacing);
                int nodeIndex = asNodeIndex(index);
                getChildren().addAll(nodeIndex, replacing);
            }
        }
    }


    public PathElement[] caretShape(int charIndex, boolean leading) {
        if (charIndex == 0) {
            if (lines.isEmpty() || (lines.size() == 1 && lineString(0).isEmpty())) {
                return blankText.caretShape(0, leading);
            }
        }
        return super.caretShape(charIndex, leading);
    }


    public List<Point2D> linePoints() {
        List<Point2D> points = new ArrayList<>();
        int offset = 0;
        double prev = 0;
        for (List<Text> line :lines) {
            PathElement[] paths = rangeShape(offset, offset + 1);
            if (lines.size() == 1 && paths.length == 0) {
                points.add(new Point2D(0, 0));
            } else {
                double y = (paths == null || paths.length == 0)
                    ? prev + Utils.getTextHeight(Fonts.main)
                    : Utils.getPathMinY(paths);
                points.add(new Point2D(0, y));
                prev = y;
            }
            offset += line.stream().map(Text::getText).mapToInt(String::length).sum();
        }
        return points;
    }


    private List<Text> asTexts(int line, String text) {
        return highlighter.apply(line, text).stream()
            .map(p -> asText(p.text(), p.paint()))
            .collect(Collectors.toList());
    }


    private Text asText(String string, Paint paint) {
        Text text = new Text(string);
        text.setTextOrigin(VPos.TOP);
        text.setFont(Fonts.main);
        text.setFill(paint);
        text.setFontSmoothingType(FontSmoothingType.LCD);
        return text;
    }


    private int asNodeIndex(int index) {
        int nodeIndex = 0;
        for (int i = 0; i < index; i++) {
            nodeIndex += lines.get(i).size();
        }
        return nodeIndex;
    }


    private void fitPrefWidth(List<Text> texts) {
        double width = texts.stream().mapToDouble(Utils::getTextWidth).sum() + textWidth * 2;
        if (width > getWidth()) {
            setPrefWidth(width);
            setWidth(width);
        }
    }


    public String linesString() {
        return lines.stream().map(this::asString).collect(Collectors.joining());
    }

    public String lineString(int n) {
        if (n < 0 || n > lines.size() - 1) {
            return "";
        }
        return asString(lines.get(n));
    }


    private String asString(List<Text> line) {
        return line.stream().map(Text::getText).collect(Collectors.joining());
    }

    record Dirty(List<List<Text>> dirtyLines, boolean invalidated, int lineNumber, int index) {
        int originLineNumber() { return lineNumber - index; }
        void removeAll(List<List<Text>> removing) {
            // remove by identity
            dirtyLines.removeIf(d -> removing.stream().anyMatch(r -> r == d));
        }
    }

}
