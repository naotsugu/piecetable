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
package com.mammb.code.editor.ui;

import javafx.scene.shape.PathElement;

/**
 * ScreenText.
 * @author Naotsugu Kobayashi
 */
public interface ScreenText {

    /**
     * Get the char value at the specified index.
     * The first char value is at index 0.
     * @param index the index of the char value
     * @return the char value at the specified index of this string.
     */
    char charAt(int index);

    /**
     * Get the length of the text string contained in the text.
     * @return the length of the text string contained in the text
     */
    int textLength();

    /**
     * Gets shape of caret in local coordinates.
     * @param charIndex the character index for the caret
     * @param leading whether the caret is biased on the leading edge of the character
     * @return an array of PathElement which can be used to create a Shape
     */
    PathElement[] caretShape(int charIndex, boolean leading);

    /**
     * Gets shape for the range of the text in local coordinates.
     * @param start the beginning character index for the range
     * @param end the end character index (non-inclusive) for the range
     * @return an array of {@code PathElement} which can be used to create a {@code Shape}
     */
    PathElement[] rangeShape(int start, int end);

    /**
     * Get the insertion index.
     * @param x the specified point x
     * @param y the specified point y
     * @return the index of the insertion position
     */
    int insertionIndexAt(double x, double y);

}
