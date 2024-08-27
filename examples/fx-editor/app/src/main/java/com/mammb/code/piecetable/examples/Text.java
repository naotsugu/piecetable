package com.mammb.code.piecetable.examples;

import com.mammb.code.piecetable.examples.Style.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Text {

    int row();
    String text();
    double lineHeight();
    double width();
    double[] advances();
    Styles styles();
    List<StyledText> styledTexts();
    int textLength();
    boolean isHighSurrogate(int index);
    boolean isLowSurrogate(int index);

    static Text of(int row, String text, double[] advances, double lineHeight) {
        return new TextRow(row, text, advances, lineHeight);
    }

    record TextRow(
        int row, String text, double[] advances, Styles styles, double lineHeight, double width) implements Text {
        private TextRow(int row, String text, double[] advances, double lineHeight) {
            this(row, text, advances, new Styles(), lineHeight, Arrays.stream(advances).sum());
        }
        @Override
        public List<StyledText> styledTexts() { return styles.apply(text, advances); }
        @Override
        public int textLength() {
            if (text.length() >= 2 && text.charAt(text.length() - 2) == '\r' && text.charAt(text.length() - 1) == '\n') {
                return text.length() - 2;
            }
            if (!text.isEmpty() && text.charAt(text.length() - 1) == '\n') {
                return text.length() - 1;
            }
            return text.length();
        }
        @Override
        public boolean isHighSurrogate(int index) { return Character.isHighSurrogate(text.charAt(index)); }
        @Override
        public boolean isLowSurrogate(int index) { return Character.isLowSurrogate(text.charAt(index)); }
    }

    static double[] advances(String text, FontMetrics fm) {
        double[] advances = new double[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char ch1 = text.charAt(i);
            if (Character.isHighSurrogate(ch1)) {
                advances[i] = fm.getAdvance(ch1, text.charAt(i + 1));
                i++;
            } else if (Character.isISOControl(ch1)) {
                i++;
            } else if (ch1 == '\t') {
                advances[i] = fm.getAdvance(" ".repeat(4));
            } else {
                advances[i] = fm.getAdvance(ch1);
            }
        }
        return advances;
    }

    class RowDecorator {
        private static final Decorator TERMINAL = text -> text;
        private final Map<Integer, Decorator> map = new HashMap<>();
        private Decorator defaultDecorator;

        public RowDecorator(Decorator defaultDecorator) {
            this.defaultDecorator = (defaultDecorator == null) ? TERMINAL : defaultDecorator;
        }
        public static RowDecorator of(Syntax syntax) {
            return new RowDecorator(new SyntaxDecorator(syntax));
        }
        public Text apply(Text text) {
            return map.getOrDefault(text.row(), defaultDecorator).text(text);
        }
        public void clear() { map.clear(); }
        public interface Decorator {
            Text text(Text text);
        }

        private static abstract class ChainedDecorator implements Decorator {
            private Decorator self;
            private Decorator next = TERMINAL;
            public ChainedDecorator(Decorator self) {
                this.self = self;
            }
            @Override public final Text text(Text text) {
                return next.text(self.text(text));
            }
            public void add(Decorator that) {
                if (that instanceof ChainedDecorator chain) {
                    chain.next = this.next;
                    this.next = chain;
                } else {
                    this.next = that;
                }
            }
        }
        private static class SyntaxDecorator extends ChainedDecorator {
            public SyntaxDecorator(Syntax syntax) {
                super(textRow -> {
                    textRow.styles().putAll(syntax.apply(textRow.row(), textRow.text()));
                    return textRow;
                });
            }
        }
        private static class AccentDecorator extends ChainedDecorator {
            public AccentDecorator() {
                super(TERMINAL);
            }
        }
    }

}
