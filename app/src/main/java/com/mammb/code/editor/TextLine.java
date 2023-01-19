package com.mammb.code.editor;

import com.mammb.code.syntax.Highlighter;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TextLine extends TextFlow {

    private List<List<Text>> lines = new ArrayList<>();
    private double textWidth = Utils.getTextWidth(Fonts.main, 1);
    private Highlighter highlighter;

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
        int nodeIndex = asNodeIndex(index);
        List<List<Text>> adding = texts.stream().map(text -> asTexts(lineNumber, text)).toList();
        lines.addAll(index, adding);
        adding.forEach(this::fitPrefWidth);
        getChildren().addAll(nodeIndex, adding.stream().flatMap(Collection::stream).toList());
    }


    public void remove(int lineNumber, int from, int to) {
        List<List<Text>> removing = lines.subList(from, to);
        List<Text> nodes = removing.stream().flatMap(Collection::stream).toList();
        lines.removeAll(removing);
        getChildren().removeAll(nodes);
    }


    public void handleDirty(int line) {
        highlighter.invalidAfter(line);
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
        return lines.stream().map(this::asText).collect(Collectors.joining());
    }

    public String lineText(int n) {
        if (n < 0 || n > lines.size() - 1) {
            return "";
        }
        return asText(lines.get(n));
    }


    private String asText(List<Text> line) {
        return line.stream().map(Text::getText).collect(Collectors.joining());
    }

}
