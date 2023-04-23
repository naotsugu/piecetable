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
package com.mammb.code.editor3.model;

/**
 * Decorated.
 * @author Naotsugu Kobayashi
 */
public interface Decorated {

    /**
     * Get the size.
     * @return the size
     */
    int size();

    /**
     * Get the color.
     * @return the color
     */
    Coloring color();

    /**
     * Get the bold.
     * @return the bold
     */
    boolean bold();

    /**
     * Get the underline.
     * @return the underline
     */
    boolean underLine();

    /**
     * Get the italic.
     * @return the italic
     */
    boolean italic();

    /**
     * Get whether this text is normal.
     * @return {@code true}, if this text is normal
     */
    default boolean normal() {
        return !bold() && !underLine() && !italic();
    }


    /**
     * Create a new decorated.
     * @param color the color
     * @return a new decorated
     */
    static Decorated of(Coloring color) {
        return new DecoratedRec(16, color, false, false, false);
    }


    /**
     * Create a new decorated.
     * @param size the size
     * @param color the color
     * @return a new decorated
     */
    static Decorated of(int size, Coloring color) {
        return new DecoratedRec(size, color, false, false, false);
    }


    /**
     * Decorated impl.
     * @param size the size
     * @param color the color
     * @param bold the bold
     * @param underLine the underLine
     * @param italic the italic
     */
    record DecoratedRec(
        int size,
        Coloring color,
        boolean bold,
        boolean underLine,
        boolean italic) implements Decorated { }

}
