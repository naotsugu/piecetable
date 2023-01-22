package com.mammb.code.syntax;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Scope {

    private TreeMap<LinePoint, ScopeMark> tree = new TreeMap<>();


    public void push(ScopeMark mark) {
        tree.put(mark.linePoint(), mark);
        pairing(mark.name());
    }

    public boolean within(LinePoint point, String name) {
        for (LinePoint i = tree.floorKey(point); i != null; i = tree.lowerKey(i)) {
            ScopeMark mark = tree.get(i);
            if (!mark.name().equals(name)) continue; // skip different mark

            if (mark.isEnd()) {
                if (mark.hasPair() && mark.linePoint().line() == point.line() &&
                    mark.linePoint().position() <= point.position() && point.position() < mark.edgePosition()) {
                    //         + mark.linePoint().position()
                    //  |.|.|.|*|/|.|
                    //             L mark.edgePosition()
                    return true;
                }
                break;
            } else if (!mark.hasPair()) {
                // Start point with no end mark
                return true;
            }
            LinePoint end = mark.pair().linePoint();
            if (end.line() > point.line() || (end.line() == point.line() && end.position() >= point.position())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsEdge(int line, int length) {
        if (tree.isEmpty()) {
            return false;
        }
        LinePoint next = tree.ceilingKey(new LinePoint(line, 0));
        if (next == null) {
            return false;
        }
        return next.line() <= line + length;
    }


    private void pairing(String name) {
        Deque<ScopeMark> stack = new ArrayDeque<>();
        for (ScopeMark mark : tree.values()) {
            if (!mark.name().equals(name)) continue;
            mark.unlink();
            if (mark.isStart() || mark.isNeutral()) {
                stack.push(mark);
            } else if (!stack.isEmpty()) {
                stack.pop().linkTo(mark);
            }
        }
    }


    public void removeCeiling(int lineStart) {

        if (tree.isEmpty() || lineStart > highWaterLine()) {
            return;
        }

        LinePoint first = new LinePoint(lineStart, 0);
        LinePoint last = new LinePoint(highWaterLine(), Integer.MAX_VALUE);

        var entries = tree.subMap(first, last).entrySet();
        entries.forEach(e -> e.getValue().unlink());

        List<LinePoint> removing = entries.stream().map(Map.Entry::getKey).toList();
        removing.forEach(tree::remove);

    }


    public int highWaterLine() {
        if (tree.isEmpty()) return -1;
        LinePoint last = tree.lastKey();
        return (last == null) ? -1 : last.line();
    }

    public int size() {
        return tree.size();
    }

}
