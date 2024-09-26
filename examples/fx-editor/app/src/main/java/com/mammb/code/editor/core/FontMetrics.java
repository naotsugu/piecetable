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
package com.mammb.code.editor.core;

/**
 * The font metrics.
 * @author Naotsugu Kobayashi
 */
public interface FontMetrics {

    /**
     * The distance from the baseline to the max character height.
     * This value is always positive
     * @return the max ascent
     */
    float getMaxAscent();

    /**
     * The distance from the baseline to the avg max character height.
     * this value is always positive
     * @return the ascent
     */
    float getAscent();

    /**
     * The distance from the baseline to the top of the avg. lowercase letter.
     * @return the x height
     */
    float getXheight();

    /**
     * The baseline is the imaginary line upon which letters without descenders
     * (for example, the lowercase letter "a") sits. In terms of the font
     * metrics, all other metrics are derived from this point. This point is
     * implicitly defined as zero.
     * @return the baseline
     */
    int getBaseline();

    /**
     * The distance from the baseline down to the lowest avg. descender.
     * This value is always positive
     * @return the descent
     */
    float getDescent();

    /**
     * The distance from the baseline down to the absolute lowest descender.
     * this value is always positive
     * @return the max descent
     */
    float getMaxDescent();

    /**
     * The amount of space between lines of text in this font. This is the
     * amount of space between he maxDecent of one line and the maxAscent
     * of the next. This number is included in the lineHeight.
     * @return the leading
     */
    float getLeading();

    /**
     * The maximum line height for a line of text in this font.
     * maxAscent + maxDescent + leading
     * @return the line height
     */
    float getLineHeight();

    /**
     * Access to individual character advances are frequently needed for layout
     * understand that advance may vary for single glyph if ligatures or kerning
     * are enabled
     * @param codePoint the code point
     * @return advance of single char
     */
    float getAdvance(int codePoint);

    /**
     * Get the total advance.
     * @param str the string
     * @return advance of string
     */
    float getAdvance(String str);

    /**
     * Get the total advance.
     * @param high the high surrogate char
     * @param low the low surrogate char
     * @return advance of char
     */
    float getAdvance(char high, char low);

}
