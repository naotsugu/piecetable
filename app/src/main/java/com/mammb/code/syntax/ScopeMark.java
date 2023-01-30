package com.mammb.code.syntax;

import java.util.Objects;

public class ScopeMark {

    private enum Type { START, END, NEUTRAL }

    private final String name;
    private final Type type;
    private final LinePoint linePoint;
    private final int size;
    private ScopeMark pair;

    private ScopeMark(String name, Type type, LinePoint linePoint, int size, ScopeMark pair) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.linePoint = Objects.requireNonNull(linePoint);
        this.size = size;
        this.pair = pair;
    }

    public static ScopeMark of(String name, LinePoint linePoint, int size) {
        return new ScopeMark(name, Type.NEUTRAL, linePoint, size, null);
    }

    public static ScopeMark startOf(String name, LinePoint linePoint, int size) {
        return new ScopeMark(name, Type.START, linePoint, size, null);
    }

    public static ScopeMark endOf(String name, LinePoint linePoint, int size) {
        return new ScopeMark(name, Type.END, linePoint, size, null);
    }

    public void linkTo(ScopeMark other) {
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
    public boolean isNeutral() { return type == Type.NEUTRAL; }
    public int edgePosition() { return (type == Type.END) ? linePoint.position() + size : linePoint.position(); }

    public String name() { return name; }
    public LinePoint linePoint() { return linePoint; }
    public ScopeMark pair() { return pair; }
    public int size() { return size; }

    @Override
    public String toString() {
        return "name:" + name + ", type:" + type + ", linePoint:" + linePoint + ", size:" + size + ", pair:" + pair.hashCode();
    }

}
