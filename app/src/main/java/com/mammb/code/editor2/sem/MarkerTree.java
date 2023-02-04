package com.mammb.code.editor2.sem;

import com.mammb.code.editor2.model.LinePoint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.TreeMap;

public class MarkerTree {

    private final TreeMap<LinePoint, Marker> tree = new TreeMap<>();

    public void push(Marker marker) {
        tree.put(marker.linePoint(), marker);
        pairing(marker);
    }

    private void pairing(Marker marker) {
        Deque<Marker> stack = new ArrayDeque<>();
        for (Marker val : sameMarkers(marker)) {
        }
    }

    private List<Marker> sameMarkers(Marker kind) {
        return tree.values().stream().filter(kind::isKindOf).toList();
    }

}
