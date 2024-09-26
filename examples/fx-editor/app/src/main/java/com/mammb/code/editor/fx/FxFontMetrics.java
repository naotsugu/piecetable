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

import com.mammb.code.editor.core.FontMetrics;
import com.sun.javafx.font.CharToGlyphMapper;
import com.sun.javafx.font.FontResource;
import com.sun.javafx.font.FontStrike;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.text.FontHelper;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Font;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FxFontMetrics implements FontMetrics {

    /** The font loader. */
    private final FontLoader fontLoader;
    /** The font metrics. */
    private final com.sun.javafx.tk.FontMetrics fontMetrics;
    /** The font that was used to construct these metrics. */
    private final Font font;

    private final FontStrike strike;
    private final FontResource resource;
    private final CharToGlyphMapper mapper;
    private final Map<Integer, Float> advanceCache = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param font the font that was used to construct these metrics
     */
    FxFontMetrics(Font font) {
        this.fontLoader = Toolkit.getToolkit().getFontLoader();
        this.font = Objects.requireNonNull(font);
        this.fontMetrics = fontLoader.getFontMetrics(font);

        var pgFont = (PGFont) FontHelper.getNativeFont(font);
        this.strike = pgFont.getStrike(BaseTransform.IDENTITY_TRANSFORM, FontResource.AA_GREYSCALE);
        this.resource = strike.getFontResource();
        this.mapper  = resource.getGlyphMapper();
    }

    public static FxFontMetrics of(Font font) {
        return new FxFontMetrics(font);
    }

    /**
     * The font that was used to construct these metrics.
     * @return the font
     */
    public final Font getFont() {
        return font;
    }

    @Override
    public final float getMaxAscent() {
        return fontMetrics.getMaxAscent();
    }

    @Override
    public final float getAscent() {
        return fontMetrics.getAscent();
    }

    @Override
    public final float getXheight() {
        return fontMetrics.getXheight();
    }

    @Override
    public final int getBaseline() {
        return fontMetrics.getBaseline();
    }

    @Override
    public final float getDescent() {
        return fontMetrics.getDescent();
    }

    @Override
    public final float getMaxDescent() {
        return fontMetrics.getMaxDescent();
    }

    @Override
    public final float getLeading() {
        return fontMetrics.getLeading();
    }

    @Override
    public final float getLineHeight() {
        return fontMetrics.getLineHeight();
    }

    @Override
    public float getAdvance(int codePoint) {
        return advanceCache.computeIfAbsent(codePoint,
                cp -> resource.getAdvance(mapper.charToGlyph(cp), strike.getSize()));
    }

    @Override
    public float getAdvance(String str) {
        return (float) str.codePoints().mapToDouble(this::getAdvance).sum();
    }

    @Override
    public float getAdvance(char high, char low) {
        return getAdvance(Character.toCodePoint(high, low));
    }
}
