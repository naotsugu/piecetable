package com.mammb.code.editor;

import com.mammb.code.syntax.Java;
import com.mammb.code.trie.Range;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TextLine extends TextFlow {

    private List<List<Text>> lines = new ArrayList<>();

    public TextLine() {
        setTabSize(4);
        setMaxWidth(Double.MAX_VALUE);
        add(" ");
    }

    public void add(String text) {
        List<Text> texts = asTexts(text);
        lines.add(texts);
        fitPrefWidth(texts);
        getChildren().addAll(texts);
    }


    public void addAll(int index, Collection<? extends String> collection) {
        int nodeIndex = asNodeIndex(index);
        List<List<Text>> adding = collection.stream().map(this::asTexts).toList();
        lines.addAll(index, adding);
        adding.forEach(this::fitPrefWidth);
        getChildren().addAll(nodeIndex, adding.stream().flatMap(Collection::stream).toList());
    }


    public void remove(int from, int to) {
        List<List<Text>> removing = lines.subList(from, to);
        List<Text> nodes = removing.stream().flatMap(Collection::stream).toList();
        lines.removeAll(removing);
        getChildren().removeAll(nodes);
    }


    private Text asText(String string) {
        return asText(string, Colors.fgColor);
    }

    private Text asText(String string, Paint paint) {
        Text text = new Text(string);
        text.setFont(Fonts.main);
        text.setFill(paint);
        return text;
    }


    private List<Text> asTexts(String string) {

        List<Text> list = new ArrayList<>();
        int offset = 0;
        for (Range range : Java.keywordRangeOf(string)) {
            if (offset != range.start()) {
                list.add(asText(string.substring(offset, range.start())));
            }
            list.add(asText(string.substring(range.start(), range.endExclusive()), Colors.kwColor));
            offset = range.endExclusive();
        }
        if (offset < string.length()) {
            list.add(asText(string.substring(offset)));
        }
        return list;
    }


    private int asNodeIndex(int index) {
        int nodeIndex = 0;
        for (int i = 0; i < index; i++) {
            nodeIndex += lines.get(i).size();
        }
        return nodeIndex;
    }

    private void fitPrefWidth(List<Text> texts) {
        double width = Fonts.main.getSize() * texts.stream().mapToInt(text -> text.getText().length()).sum();
        if (width > getPrefWidth()) {
            setPrefWidth(width + Fonts.main.getSize());
        }
//        double width = texts.stream().mapToDouble(Utils::getTextWidth).sum();
//        if (width > getPrefWidth()) {
//            setPrefWidth(width + 8);
//        }
    }

}
