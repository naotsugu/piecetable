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

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Color utilities.
 * @author Naotsugu Kobayashi
 */
public class Colors {

    public static final Color background = Color.web("#2B2B2B");

    public static final Color foreground = Color.web("#DDDDDD");

    public static final Color foregroundModest = Color.web("#939ea9");

    public static final Color panel = Color.web("#313335");


    public static final Color scrollTrack = Color.web("#626465", 0.2);
    public static final Paint thumb = Color.web("#626465", 0.5);
    public static final Paint thumbActive = Color.web("#828485", 0.9);


    public static Color of(int index) {
        return switch (index) {
            case 3 -> Color.web("#eb8a3a");
            case 6 -> Color.web("#808080");
            case 7 -> Color.web("#629755");
            default -> Colors.foreground;
        };
    }

}
