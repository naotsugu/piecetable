package com.mammb.code.editor;

import javafx.scene.shape.PathElement;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import java.util.Arrays;
import java.util.Comparator;

public class PathElements {

    public static double getHeight(PathElement... elements) {
        return getPathMaxY(elements) - getPathMinY(elements);
    }

    public static double getPathMinX(PathElement... elements) {
        return Arrays.stream(elements).map(PathElements::getPathX).min(Comparator.naturalOrder()).orElse(0.0);
    }

    public static double getPathMaxY(PathElement... elements) {
        return Arrays.stream(elements).map(PathElements::getPathY).max(Comparator.naturalOrder()).orElse(0.0);
    }

    public static double getPathMinY(PathElement... elements) {
        return Arrays.stream(elements).map(PathElements::getPathY).min(Comparator.naturalOrder()).orElse(0.0);
    }

    public static double getPathX(PathElement element) {
        if (element instanceof MoveTo moveTo) return moveTo.getX();
        if (element instanceof LineTo lineTo) return lineTo.getX();
        throw new UnsupportedOperationException(element.toString());
    }

    public static double getPathY(PathElement element) {
        if (element instanceof MoveTo moveTo) return moveTo.getY();
        if (element instanceof LineTo lineTo) return lineTo.getY();
        throw new UnsupportedOperationException(element.toString());
    }

}
