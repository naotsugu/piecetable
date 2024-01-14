/*
 * Copyright 2022-2024 the original author or authors.
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
package com.mammb.code.editor.ui.util;

import javafx.scene.shape.*;
import java.util.Arrays;

/**
 * Utility of {@link PathElement}.
 * @author Naotsugu Kobayashi
 */
public class PathElements {


    /**
     * Get the width of the path elements.
     * @param elements the path elements
     * @return the width
     */
    public static double width(PathElement... elements) {
        return Arrays.stream(elements).mapToDouble(PathElements::getX).max().orElse(0.0) -
               Arrays.stream(elements).mapToDouble(PathElements::getX).min().orElse(0.0);
    }


    /**
     * Get the height of the path elements.
     * @param elements the path elements
     * @return the height
     */
    public static double height(PathElement... elements) {
        return Arrays.stream(elements).mapToDouble(PathElements::getY).max().orElse(0.0) -
               Arrays.stream(elements).mapToDouble(PathElements::getY).min().orElse(0.0);
    }


    /**
     * Get the x position of the path element.
     * @param element the path element
     * @return the x position
     */
    public static double getX(PathElement element) {
        if (element instanceof MoveTo moveTo) return moveTo.getX();
        else if (element instanceof LineTo lineTo) return lineTo.getX();
        else if (element instanceof HLineTo hLineTo) return hLineTo.getX();
        else throw new UnsupportedOperationException(element.toString());
    }


    /**
     * Get the y position of the path element.
     * @param element the path element
     * @return the y position
     */
    public static double getY(PathElement element) {
        if (element instanceof MoveTo moveTo) return moveTo.getY();
        else if (element instanceof LineTo lineTo) return lineTo.getY();
        else if (element instanceof VLineTo vLineTo) return vLineTo.getY();
        else throw new UnsupportedOperationException(element.toString());
    }

}
