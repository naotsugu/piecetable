package com.mammb.code.editor;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.Comparator;

public class Utils {

    public static double getTextHeight(Text text) {
        return getHeight(text.rangeShape(0, 1));
    }

    public static double getTextWidth(Text text) {
        return getWidth(text.rangeShape(0, text.getText().length()));
    }

    public static double getTextHeight(Font font) {
        Text sample = new Text("0");
        sample.setFont(font);
        return getTextHeight(sample);
    }

    public static double getTextWidth(Font font, int n) {
        Text sample = new Text("0".repeat(n));
        sample.setFont(font);
        return getTextWidth(sample);
    }

    private static double getWidth(PathElement... elements) {
        return getPathMaxX(elements) - getPathMinX(elements);
    }

    private static double getHeight(PathElement... elements) {
        return getPathMaxY(elements) - getPathMinY(elements);
    }

    private static double getPathMinX(PathElement... elements) {
        return Arrays.stream(elements).map(Utils::getPathX).min(Comparator.naturalOrder()).orElse(0.0);
    }
    private static double getPathMaxX(PathElement... elements) {
        return Arrays.stream(elements).map(Utils::getPathX).max(Comparator.naturalOrder()).orElse(0.0);
    }

    private static double getPathMinY(PathElement... elements) {
        return Arrays.stream(elements).map(Utils::getPathY).min(Comparator.naturalOrder()).orElse(0.0);
    }
    private static double getPathMaxY(PathElement... elements) {
        return Arrays.stream(elements).map(Utils::getPathY).max(Comparator.naturalOrder()).orElse(0.0);
    }

    private static double getPathX(PathElement element) {
        if (element instanceof MoveTo moveTo) return moveTo.getX();
        else if (element instanceof LineTo) return ((LineTo) element).getX();
        else throw new UnsupportedOperationException(element.toString());
    }

    private static double getPathY(PathElement element) {
        if (element instanceof MoveTo moveTo) return moveTo.getY();
        else if (element instanceof LineTo lineTo) return lineTo.getY();
        else throw new UnsupportedOperationException(element.toString());
    }
}
