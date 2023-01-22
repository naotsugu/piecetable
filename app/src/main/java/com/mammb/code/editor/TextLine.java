package com.mammb.code.editor;

import com.mammb.code.syntax.Highlighter;
import javafx.geometry.Insets;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.ERROR;

public class TextLine extends TextFlow {

    private static final System.Logger logger = System.getLogger(TextLine.class.getName());

    private final List<List<Text>> lines = new ArrayList<>();
    private double textWidth = Utils.getTextWidth(Fonts.main, 1);
    private Highlighter highlighter;
    private Dirty dirty;

    public TextLine() {
        highlighter = Highlighter.of("");
        setPadding(new Insets(4));
        setTabSize(4);
        setMaxWidth(Double.MAX_VALUE);
        addAll(0, 0, List.of(" "));
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
        List<List<Text>> removing = new ArrayList<>(lines.subList(index, index + length));
        List<Text> nodes = removing.stream().flatMap(Collection::stream).toList();
        lines.removeAll(removing);
        if (dirty != null) dirty.removeAll(removing);
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


    private List<Text> asTexts(int line, String text) {
        return highlighter.apply(line, text).stream()
            .map(p -> asText(p.text(), p.paint())).toList();
    }


    private Text asText(String string, Paint paint) {
        Text text = new Text(string);
        text.setFont(Fonts.main);
        text.setFill(paint);
        //text.setFontSmoothingType(FontSmoothingType.GRAY);
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


    public String linesText() {
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
            dirtyLines.removeAll(removing);
        }
    }

}
