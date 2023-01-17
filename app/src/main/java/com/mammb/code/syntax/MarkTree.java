package com.mammb.code.syntax;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MarkTree {

    private TreeMap<LinePoint, Mark> map = new TreeMap<>();


    public void addStart(int line, int position, String name, int markSize) {
        Mark mark = add(new Mark(name, Mark.Type.START, line, position, markSize));
        for (LinePoint i = map.higherKey(mark.linePoint()); i != null; i = map.higherKey(i)) {
            Mark prev = map.get(i);
            if (!prev.name().equals(name)) continue;
            if (prev.isEnd()) {
                prev.linkTo(mark);
                break;
            }
        }
    }


    public void addEnd(int line, int position, String name, int markSize) {
        Mark mark = add(new Mark(name, Mark.Type.END, line, position, markSize));
        for (LinePoint i = map.lowerKey(mark.linePoint()); i != null; i = map.lowerKey(i)) {
            Mark prev = map.get(i);
            if (!prev.name().equals(name)) continue;
            if (prev.isStart()) {
                prev.linkTo(mark);
                break;
            }
        }
    }


    public int removeHigher(int line) {

        if (map.isEmpty()) return -1;

        LinePoint first = new LinePoint(line , 0);
        LinePoint last = map.lastKey();
        if (first.compareTo(last) > 0) {
            return -1;
        }

        int ceiling = map.ceilingKey(first).line();

        var entries = map.subMap(first, new LinePoint(last.line() , last.position() + 1)).entrySet();
        entries.forEach(e -> e.getValue().unlink());
        List<LinePoint> removing = entries.stream().map(Map.Entry::getKey).toList();
        removing.forEach(map::remove);

        return ceiling;
    }


    public boolean isInScope(int line, int position, String name) {
        for (LinePoint i = map.floorKey(new LinePoint(line, position)); i != null; i = map.lowerKey(i)) {
            Mark mark = map.get(i);
            if (!mark.name().equals(name)) continue; // skip different mark

            if (mark.isEnd()) {
                if (mark.hasPair() && mark.linePoint().line() == line &&
                    mark.linePoint().position() <= position && position < mark.edgePosition()) {
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
            if (end.line() > line || (end.line() == line && end.position() >= position)) {
                return true;
            }
        }
        return false;
    }


    private Mark add(Mark mark) {
        map.put(mark.linePoint(), mark);
        return mark;
    }

}
