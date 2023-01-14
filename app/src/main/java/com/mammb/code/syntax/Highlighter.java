package com.mammb.code.syntax;

import javafx.scene.paint.Paint;

import java.util.List;

public interface Highlighter {

    record PaintText(String text, Paint paint) { }

    List<PaintText> asPaintRange(String text);

    static Highlighter of(String name) {
        return switch (name) {
            case ".java" -> new Java();
            default -> new PassThrough();
        };
    }

}
