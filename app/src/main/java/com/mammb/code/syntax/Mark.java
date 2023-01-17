package com.mammb.code.syntax;

import java.util.Objects;

public class Mark {

    enum Type { START, END, NEUTRAL }

    private final String name;
    private final Type type;
    private final LinePoint linePoint;
    private Mark pair;
    private int markSize;

    public Mark(String name, Type type, LinePoint linePoint, int markSize) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.linePoint = Objects.requireNonNull(linePoint);
        this.markSize = markSize;
    }

    public Mark(String name, Type type, int line, int position, int markSize) {
        this(name, type, new LinePoint(line, position), markSize);
    }

    public void linkTo(Mark other) {
        this.pair = other;
        other.pair = this;
    }

    public void unlink() {
        if (pair != null) {
            pair.pair = null;
            pair = null;
        }
    }

    public void forwardLine(int n) { linePoint.forwardLine(n); }
    public void backForwardLine(int n) { linePoint.backForwardLine(n); }

    public void forwardPosition(int n) { linePoint.forwardPosition(n); }
    public void backForwardPosition(int n) { linePoint.backForwardPosition(n); }

    public boolean hasPair() { return pair != null; }
    public boolean isStart() { return type == Type.START; }
    public boolean isEnd() { return type == Type.END; }
    public int edgePosition() { return (type == Type.END) ? linePoint.position() + markSize : linePoint.position(); }

    public String name() { return name; }
    public LinePoint linePoint() { return linePoint; }
    public Mark pair() { return pair; }
    public int markSize() { return markSize; }

}
