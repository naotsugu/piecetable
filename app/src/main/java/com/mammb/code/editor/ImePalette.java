package com.mammb.code.editor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;

import java.util.function.Function;
import java.util.function.Supplier;

public class ImePalette extends Region {

    private final BooleanProperty imeOn = new SimpleBooleanProperty(false);
    private final Supplier<PathElement[]> locationFun;
    private final Node parent;
    private Text palette;
    private Text original;


    public ImePalette(Node parent, Supplier<PathElement[]> locationFun) {
        this.parent = parent;
        this.locationFun = locationFun;
        this.palette = new Text();
        this.original = new Text();
    }


    public InputMethodRequests createInputMethodRequests() {
        return new InputMethodRequests() {
            @Override
            public Point2D getTextLocation(int offset) {
                Bounds bounds = localToScreen(parent.getBoundsInParent());
                PathElement[] p = locationFun.get();
                if (p[p.length - 1] instanceof LineTo lineTo) {
                    return new Point2D(
                        bounds.getMinX() + lineTo.getX(),
                        bounds.getMinY() + lineTo.getY());
                } else {
                    return new Point2D(bounds.getMinX(), bounds.getMinY());
                }
            }
            @Override
            public int getLocationOffset(int x, int y) {
                return 0;
            }
            @Override
            public void cancelLatestCommittedText() {
                imeOn.set(false);
            }
            @Override
            public String getSelectedText() {
                return "";
            }
        };
    }

    public void setText(String text) {
        palette.setText(text);
        original.setLayoutX(palette.getLayoutBounds().getWidth());
    }

    public final boolean getImeOn() { return imeOn.get(); }
    void setImeOn(boolean value) { imeOn.set(value); }
    public BooleanProperty imeOnProperty() { return imeOn; }

}
