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

import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Text utilities.
 * @author Naotsugu Kobayashi
 */
public class Texts {

    private static final Font DEFAULT = new Font("System Regular", 14);

    public static final Text bit = new Text("X");
    static {
        bit.setFont(DEFAULT);
    }

    public static final Font font = bit.getFont();

    public static final double height = bit.getLayoutBounds().getHeight();

    public static final double width  = bit.getLayoutBounds().getWidth();

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
        Text text = new Text(string);
        text.setFont(font);
        text.setFill(Colors.foreground);
        return text;
    }

    public static List<Text> asText(List<String> strings) {
        return strings.stream().map(Texts::asText).toList();
    }

}
