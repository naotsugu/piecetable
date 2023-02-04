package com.mammb.code.editor2.sem;

import com.mammb.code.editor2.model.LinePoint;

import java.util.Objects;

public class Marker {
    private enum Type { OPEN, CLOSE, NEUTRAL }

    private final String name;
    private final Type type;
    private final LinePoint linePoint;
    private final int size;
    private Marker pair;

    private Marker(String name, Type type, LinePoint linePoint, int size, Marker pair) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.linePoint = Objects.requireNonNull(linePoint);
        this.size = size;
        this.pair = pair;
    }

    public static Marker startOf(String name, LinePoint linePoint, int size) {
        return new Marker(name, Type.OPEN, linePoint, size, null);
    }

    public static Marker endOf(String name, LinePoint linePoint, int size) {
        return new Marker(name, Type.CLOSE, linePoint, size, null);
    }

    public static Marker neutralOf(String name, LinePoint linePoint, int size) {
        return new Marker(name, Type.NEUTRAL, linePoint, size, null);
    }

    public void linkTo(Marker other) {
        if (!linkable(other))
            throw new IllegalArgumentException(other.toString());
        this.pair = other;
        other.pair = this;
    }

    public void unlink() {
        if (pair != null) {
            pair.pair = null;
            pair = null;
        }
    }

    public boolean isKindOf(Marker other) {
        return name.equals(other.name);
    }

    public boolean linkable(Marker other) {
        return isKindOf(other) && (
            (type == Type.NEUTRAL && other.type == Type.NEUTRAL) ||
                (type == Type.OPEN    && other.type == Type.CLOSE) ||
                (type == Type.CLOSE   && other.type == Type.OPEN)
        );
    }

    public boolean hasPair() { return pair != null; }
    public boolean isOpen() { return type == Type.OPEN; }
    public boolean isClose() { return type == Type.CLOSE; }
    public boolean isNeutral() { return type == Type.NEUTRAL; }
    public int edgePosition() { return (type == Type.CLOSE) ? linePoint.offset() + size : linePoint.offset(); }

    public String name() { return name; }
    public LinePoint linePoint() { return linePoint; }
    public Marker pair() { return pair; }
    public int size() { return size; }

}
