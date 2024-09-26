/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.editor.core;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * The Caret.
 * @author Naotsugu Kobayashi
 */
public interface Caret extends Comparable<Caret>{

    Point point();
    void at(int row, int col);
    void at(int row, int col, double vPos);
    void flushAt(int row, int col);
    void clearFlush();
    boolean hasFlush();
    Point pointFlush();
    void mark();
    void clearMark();
    boolean isMarked();
    boolean isFloating();
    Range markedRange();
    double vPos();
    default int row() {
        return point().row();
    }
    default int col() {
        return point().col();
    }
    default void at(Point point) {
        at(point.row(), point.col());
    }
    default void flushAt(Point point) {
        flushAt(point.row(), point.col());
    }
    default boolean isZero() {
        return point().isZero();
    }

    default void markIf(boolean withSelect) {
        if (!withSelect) {
            clearMark();
        } else if (!isMarked()) {
            mark();
        }
    }

    static Caret of() {
        return new CaretImpl();
    }

    static Caret of(int row, int col) {
        return new CaretImpl(row, col);
    }

    class CaretImpl implements Caret {

        private final PointMut point;
        private Point mark;
        private double vPos = -1;
        private boolean floating;
        private Point flush;

        public CaretImpl(int row, int col) {
            this.point = new PointMut(row, col);
        }

        public CaretImpl() {
            this.point = new PointMut(0, 0);
        }

        @Override
        public void mark() {
            mark = new PointMut(point.row(), point.col());
        }

        @Override
        public void clearMark() {
            mark = null;
        }

        @Override
        public boolean isMarked() {
            return mark != null;
        }

        @Override
        public boolean isFloating() {
            return floating;
        }

        @Override
        public Range markedRange() {
            return isMarked() ? new Range(new PointRec(point), mark) : null;
        }

        @Override
        public double vPos() {
            return vPos;
        }

        @Override
        public int compareTo(Caret that) {
            return this.point().compareTo(that.point());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Caret caret = (Caret) o;
            return Objects.equals(point, caret.point());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(point);
        }

        @Override
        public Point point() {
            return point;
        }

        @Override
        public void at(int row, int col) {
            point.at(row, col);
            vPos = -1;
            flush = null;
        }

        @Override
        public void at(int row, int col, double x) {
            point.at(row, col);
            vPos = x;
            flush = null;
        }

        @Override
        public void flushAt(int row, int col) {
            flush = Point.of(row, col);
        }

        @Override
        public void clearFlush() {
            flush = null;
        }

        @Override
        public boolean hasFlush() {
            return flush != null;
        }

        @Override
        public Point pointFlush() {
            return Objects.isNull(flush) ? point : flush;
        }
    }

    interface Point extends Comparable<Point> {
        int row();
        int col();
        static Point of(int row, int col) { return new PointRec(row, col); }
        default boolean isZero() {
            return row() == 0 && col() == 0;
        }
        @Override
        default int compareTo(Point that) {
            int c = Integer.compare(this.row(), that.row());
            if (c == 0) {
                return Integer.compare(this.col(), that.col());
            } else {
                return c;
            }
        }
    }

    record PointRec(int row, int col) implements Point {
        PointRec(Point p) { this(p.row(), p.col()); }
    }

    class PointMut implements Point {

        private int row, col;

        PointMut(int row, int col) {
            this.row = row;
            this.col = col;
        }

        void at(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int row() {
            return row;
        }

        public int col() {
            return col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return row == point.row() && col == point.col();
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", PointMut.class.getSimpleName() + "[", "]")
                    .add("row=" + row)
                    .add("col=" + col)
                    .toString();
        }
    }

    record Range(Point start, Point end) implements Comparable<Range> {
        public Point min() {
            return start.compareTo(end) < 0 ? start : end;
        }
        public Point max() {
            return start.compareTo(end) > 0 ? start : end;
        }

        @Override
        public int compareTo(Range o) {
            return min().compareTo(o.min());
        }
    }

}