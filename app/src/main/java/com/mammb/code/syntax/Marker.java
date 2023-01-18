package com.mammb.code.syntax;

import java.util.Objects;

public class Marker {

    enum Type { START, END, NEUTRAL }

    private final String name;
    private final Type type;
    private final LinePoint linePoint;
    private Marker pair;
    private int markSize;

    public Marker(String name, Type type, LinePoint linePoint, int markSize) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.linePoint = Objects.requireNonNull(linePoint);
        this.markSize = markSize;
    }

    public Marker(String name, Type type, int line, int position, int markSize) {
        this(name, type, new LinePoint(line, position), markSize);
    }

    public void linkTo(Marker other) {
        this.pair = other;
        other.pair = this;
    }

    public void unlink() {
        if (pair != null) {
            pair.pair = null;
            pair = null;
        }
    }

    public boolean hasPair() { return pair != null; }
    public boolean isStart() { return type == Type.START; }
    public boolean isEnd() { return type == Type.END; }
    public int edgePosition() { return (type == Type.END) ? linePoint.position() + markSize : linePoint.position(); }

    public String name() { return name; }
    public LinePoint linePoint() { return linePoint; }
    public Marker pair() { return pair; }
    public int markSize() { return markSize; }

}
