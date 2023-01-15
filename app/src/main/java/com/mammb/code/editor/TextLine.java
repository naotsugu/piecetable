package com.mammb.code.editor;

import com.mammb.code.syntax.Highlighter;
import javafx.geometry.Insets;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

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
        fitPrefWidth(texts.stream().mapToInt(String::length).max().getAsInt());
        getChildren().addAll(nodeIndex, adding.stream().flatMap(Collection::stream).toList());
    }


    public void remove(int lineNumber, int from, int to) {
        List<List<Text>> removing = lines.subList(from, to);
        List<Text> nodes = removing.stream().flatMap(Collection::stream).toList();
        lines.removeAll(removing);
        IntStream.range(lineNumber, lineNumber + (to - from)).forEach(highlighter::remove);
        getChildren().removeAll(nodes);
    }


    private List<Text> asTexts(int line, String text) {
        return highlighter.apply(line, text).stream().map(p -> asText(p.text(), p.paint())).toList();
    }


    private Text asText(String string, Paint paint) {
        Text text = new Text(string);
        text.setFont(Fonts.main);
        text.setFill(paint);
        return text;
    }


    private int asNodeIndex(int index) {
        int nodeIndex = 0;
        for (int i = 0; i < index; i++) {
            nodeIndex += lines.get(i).size();
        }
        return nodeIndex;
    }


    private void fitPrefWidth(int length) {
        double width = textWidth * length + 2;
        if (width > getPrefWidth()) {
            setPrefWidth(width);
        }
    }

}