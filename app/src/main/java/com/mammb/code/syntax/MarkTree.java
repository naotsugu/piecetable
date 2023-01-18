package com.mammb.code.syntax;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MarkTree {

    private TreeMap<LinePoint, Marker> map = new TreeMap<>();


    public void addStart(int line, int position, String name, int markSize) {
        Marker marker = add(new Marker(name, Marker.Type.START, line, position, markSize));
        for (LinePoint i = map.higherKey(marker.linePoint()); i != null; i = map.higherKey(i)) {
            Marker prev = map.get(i);
            if (prev.name().equals(name) && prev.isEnd()) {
                prev.linkTo(marker);
                break;
            }
        }
    }


    public void addEnd(int line, int position, String name, int markSize) {
        Marker marker = add(new Marker(name, Marker.Type.END, line, position, markSize));
        for (LinePoint i = map.lowerKey(marker.linePoint()); i != null; i = map.lowerKey(i)) {
            Marker prev = map.get(i);
            if (prev.name().equals(name) && prev.isStart()) {
                prev.linkTo(marker);
                break;
            }
        }
    }


    public void sluice(int lineStart) {

        if (map.isEmpty() || lineStart > highWaterMark()) {
            return;
        }

        LinePoint first = new LinePoint(lineStart , 0);
        LinePoint last = new LinePoint(highWaterMark() , Integer.MAX_VALUE);

        var entries = map.subMap(first, last).entrySet();
        entries.forEach(e -> e.getValue().unlink());

        List<LinePoint> removing = entries.stream().map(Map.Entry::getKey).toList();
        removing.forEach(map::remove);

    }


    public boolean isInScope(int line, int position, String name) {
        for (LinePoint i = map.floorKey(new LinePoint(line, position)); i != null; i = map.lowerKey(i)) {
            Marker marker = map.get(i);
            if (!marker.name().equals(name)) continue; // skip different mark

            if (marker.isEnd()) {
                if (marker.hasPair() && marker.linePoint().line() == line &&
                    marker.linePoint().position() <= position && position < marker.edgePosition()) {
                    //         + mark.linePoint().position()
                    //  |.|.|.|*|/|.|
                    //             L mark.edgePosition()
                    return true;
                }
                break;
            } else if (!marker.hasPair()) {
                // Start point with no end mark
                return true;
            }
            LinePoint end = marker.pair().linePoint();
            if (end.line() > line || (end.line() == line && end.position() >= position)) {
                return true;
            }
        }
        return false;
    }


    private Marker add(Marker marker) {
        map.put(marker.linePoint(), marker);
        return marker;
    }


    public int highWaterMark() {
        if (map.isEmpty()) {
            return -1;
        }
        LinePoint last = map.lastKey();
        return (last == null) ? -1 : last.line();
    }

}
