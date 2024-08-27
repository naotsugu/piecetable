package com.mammb.code.piecetable.examples;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Style {

    record TextColor(String colorString) implements Style {}
    record BgColor(String colorString) implements Style {}
    record Selected() implements Style {}
    record Emphasize() implements Style {}
    record UnderLine(String colorString, double dash) implements Style {}

    record StyleSpan(Style style, int offset, int length) { }
    record StyledText(String text, double width, List<Style> styles) { }

    class Styles {
        private final Set<Integer> bounds = new HashSet<>();
        private final List<StyleSpan> spans = new ArrayList<>();
        void putAll(List<StyleSpan> spans) {
            spans.forEach(this::put);
        }
        void put(StyleSpan span) {
            bounds.add(span.offset);
            bounds.add(span.offset + span.length);
            spans.add(span);
        }
        List<StyledText> apply(String text, double[] advances) {
            return apply(0, text.length(), text, advances);
        }
        List<StyledText> apply(int from, int to, String text, double[] advances) {
            assert text.length() == advances.length;

            List<Integer> list = bounds.stream()
                .filter(i -> from <= i && i <= to)
                .sorted()
                .collect(Collectors.toList());

            if (list.isEmpty()) {
                return List.of(new StyledText(
                    text.substring(from, to),
                    width(advances, from, to),
                    List.of()));
            }

            if (list.getFirst() != from) {
                list.addFirst(from);
            }
            if (list.getLast() != to) {
                list.addLast(to);
            }
            List<StyledText> ret = new ArrayList<>();
            for (int i = 0; i < list.size() - 1; i++) {
                int start = list.get(i);
                int end   = list.get(i + 1);
                ret.add(new StyledText(
                    text.substring(start, end),
                    width(advances, start, end),
                    stylesOf(list.get(i))));
            }
            return ret;

        }
        private List<Style> stylesOf(int index) {
            return spans.stream()
                .filter(span -> span.offset <= index && index < (span.offset + span.length))
                .map(span -> span.style)
                .toList();
        }
    }

    private static double width(double[] advances, int from, int to) {
        double ret = 0;
        for (int i = from; i < to; i++) ret += advances[i];
        return ret;
    }
}
