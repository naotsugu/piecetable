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
package com.mammb.code.editor.model;

/**
 * DecoratedText.
 * @author Naotsugu Kobayashi
 */
public interface DecoratedText extends Decorating {

    /**
     * Get the text.
     * @return the text
     */
    String text();


    /**
     * Create a new DecoratedText.
     * @param text the text
     * @param decorating the decorated
     * @return a new DecoratedText
     */
    static DecoratedText of(String text, Decorating decorating) {
        return new DecoratedTextRec(text,
            decorating.size(),
            decorating.color(),
            decorating.bold(),
            decorating.underLine(),
            decorating.italic());
    }


    /**
     * DecoratedText impl.
     * @param text the text
     * @param size the size
     * @param color the color
     * @param bold the bold
     * @param underLine the underLine
     * @param italic the italic
     */
    record DecoratedTextRec(
        String text,
        double size,
        Coloring color,
        boolean bold,
        boolean underLine,
        boolean italic) implements DecoratedText { }

}


