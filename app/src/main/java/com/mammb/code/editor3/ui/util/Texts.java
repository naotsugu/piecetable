/*
 * Copyright 2019-2023 the original author or authors.
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
package com.mammb.code.editor3.ui.util;

import com.mammb.code.editor3.model.DecoratedText;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.List;

/**
 * Text utilities.
 * @author Naotsugu Kobayashi
 */
public class Texts {

    private static final Font DEFAULT = new Font("Consolas", 16);

    public static final Text bit = new Text("X");
    static {
        bit.setFont(DEFAULT);
    }

    public static final Font font = bit.getFont();

    public static final double height = PathElements.height(bit.caretShape(1, true));

    public static final double width = PathElements.width(bit.rangeShape(0, 1));


    /**
     * Split a string with specified wrapping width.
     * @param string a string
     * @param wrappingWidth wrapping width
     * @return split string
     */
    public static String[] split(String string, float wrappingWidth) {

        if (string == null || string.isEmpty()) {
            return new String[0];
        }

        Text text = asText(string);
        text.setWrappingWidth(wrappingWidth);
        int line = (int) (text.getLayoutBounds().getHeight() / height);
        if (string.charAt(string.length() - 1) == '\n') line--;

        String[] ret = new String[line];
        int offset = 0;
        for (int i = 0; i < line; i++) {
            int end = (i < line - 1)
                ? text.hitTest(new Point2D(Double.MAX_VALUE, i * height)).getInsertionIndex()
                : string.length();
            ret[i] = string.substring(offset, end);
            offset = end;
        }
        return ret;
    }


    public static Text asText(String string) {
        return asText(string, Colors.foreground);
    }


    public static List<Text> asText(List<DecoratedText> texts) {
        return texts.stream().map(Texts::asText).toList();
    }


    public static Text asText(DecoratedText txt) {

        Text text = new Text(txt.text());
        if (txt.size() == font.getSize() && txt.normal()) {
            text.setFont(font);
        } else {
            text.setFont(Font.font(
                font.getName(),
                txt.bold() ? FontWeight.BOLD : FontWeight.MEDIUM,
                txt.italic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                txt.size()));
            if (txt.underLine()) {
                text.setUnderline(true);
            }
        }
        text.setFill(Colors.of(txt.color()));
        return text;
    }


    public static Text asText(String string, Color color) {
        Text text = new Text(string);
        text.setFont(font);
        text.setFill(color);
        return text;
    }


    public static List<Text> asTextPlain(List<String> strings) {
        return strings.stream().map(Texts::asText).toList();
    }


    /**
     * Create the copy of text.
     * @param source the source
     * @return the copy
     */
    public static Text copy(Text source) {
        Text copy = new Text(source.getText());
        copy.setFont(source.getFont());
        copy.setFill(source.getFill());
        return copy;
    }


    public static double width(Text text) {
        return PathElements.width(text.rangeShape(0, text.getText().length()));
    }

    public static double width(String string, Font font) {
        Text text = new Text(string);
        text.setFont(font);
        return width(text);
    }

}
