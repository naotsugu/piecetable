package com.mammb.code.syntax;

import java.util.Objects;

public class LinePoint implements Comparable<LinePoint> {

    private int line;
    private int position;

    public LinePoint(int line, int position) {
        if (line < 0 || position < 0) {
            throw new IllegalArgumentException();
        }
        this.line = line;
        this.position = position;
    }

    public int line() {
        return line;
    }

    public int position() {
        return position;
    }

    void forwardLine(int n) {
        if (n < 0) throw new IllegalArgumentException();
        line += n;
    }

    void backForwardLine(int n) {
        if (n < 0) throw new IllegalArgumentException();
        line -= n;
    }

    void forwardPosition(int n) {
        if (n < 0) throw new IllegalArgumentException();
        position += n;
    }

    void backForwardPosition(int n) {
        if (n < 0) throw new IllegalArgumentException();
        position -= n;
    }

    @Override
    public int compareTo(LinePoint o) {
        int compare = Integer.compare(this.line, o.line);
        return (compare == 0) ? Integer.compare(this.position, o.position) : compare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinePoint linePoint = (LinePoint) o;
        return line == linePoint.line && position == linePoint.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, position);
    }

}
