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
package com.mammb.code.editor.fx;

import com.mammb.code.editor.core.text.Style;
import com.mammb.code.editor.core.Draw;
import com.mammb.code.editor.core.FontMetrics;
import com.mammb.code.editor.core.Theme;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FxDraw implements Draw {
    private final GraphicsContext gc;
    private final FontMetrics fontMetrics;
    private final Map<String, Color> colors = new HashMap<>();

    public FxDraw(GraphicsContext gc) {
        this.gc = gc;
        Font font = defaultFont();
        this.fontMetrics = FxFontMetrics.of(font);
        this.gc.setFont(font);
    }

    @Override
    public void clear() {
        Canvas canvas = gc.getCanvas();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void text(String text, double x, double y, double w, List<Style> styles) {
        Color color = color(styles.stream().filter(Style.TextColor.class::isInstance)
                .map(Style.TextColor.class::cast).findFirst()
                .map(Style.TextColor::colorString).orElse(Theme.dark.fgColor()));
        gc.setStroke(color);
        gc.setFill(color);
        gc.fillText(text, x, y + fontMetrics.getAscent());
    }

    @Override
    public void caret(double x, double y) {
        gc.setLineDashes(0);
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(1.5);
        gc.strokeLine(x - 1, y, x - 1, y + fontMetrics.getLineHeight());
    }

    @Override
    public void select(double x1, double y1, double x2, double y2, double l, double r) {
        double lineHeight = fontMetrics().getLineHeight();
        gc.setFill(color(Theme.dark.paleHighlightColor()));
        if (y1 == y2) {
            gc.fillRect(Math.min(x1, x2), y1, Math.abs(x2 - x1), lineHeight);
            return;
        }
        //                    0:(x1, y1)
        //                     _______________________  1:(r, y1)
        // 6:(l, y1 + h) _____|                      |
        //               |   7:(x1, y1 + h)          |
        //               |                           |
        //               |     3:(x2, y2)  __________| 2:(r, y2)
        //               |________________|
        // 5:(l, y2 + h)                4:(x2, y2 + h)
        double[] x = new double[8];
        double[] y = new double[8];
        x[0] = x1; y[0]= y1;
        x[1] = r;  y[1]= y1;
        x[2] = r;  y[2]= y2;
        x[3] = x2; y[3]= y2;
        x[4] = x2; y[4]= y2 + lineHeight;
        x[5] = l;  y[5]= y2 + lineHeight;
        x[6] = l;  y[6]= y1 + lineHeight;
        x[7] = x1; y[7]= y1 + lineHeight;
        gc.fillPolygon(x, y, 8);
    }

    @Override
    public void underline(double x1, double y1, double x2, double y2) {
        double height = fontMetrics().getAscent();
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        gc.strokeLine(x1, y1 + height, x2, y2 + height);
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        Color color = color(Theme.dark.uiBaseColor());
        gc.setFill(color);
        gc.fillRect(x, y, w, h);
    }


    @Override
    public FontMetrics fontMetrics() {
        return fontMetrics;
    }

    private Font defaultFont() {
        String fontName = System.getProperty("os.name").toLowerCase().startsWith("windows")
                ? "MS Gothic" : "Consolas";
        return Font.font(fontName, 3 * 5);
    }

    private Color color(String name) {
        return colors.computeIfAbsent(name, Color::web);
    }

}