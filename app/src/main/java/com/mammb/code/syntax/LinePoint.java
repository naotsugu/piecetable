package com.mammb.code.syntax;

public record LinePoint(int line, int position) implements Comparable<LinePoint> {

    public LinePoint {
        if (line < 0 || position < 0) throw new IllegalArgumentException();
    }

    @Override
    public int compareTo(LinePoint o) {
        int compare = Integer.compare(this.line, o.line);
        return (compare == 0) ? Integer.compare(this.position, o.position) : compare;
    }

}
