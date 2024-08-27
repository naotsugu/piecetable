package com.mammb.code.piecetable.examples;

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

public interface FontMetrics {
    float getAscent();
    float getLineHeight();
    float getAdvance(int codePoint);
    float getAdvance(String str);
    float getAdvance(char high, char low);
    Object getFont();

    static FontMetrics of(String fontName, double size) {
        return new FxFontMetrics(Font.font(fontName, size));
    }

    class FxFontMetrics implements FontMetrics {
        private final com.sun.javafx.tk.FontMetrics fontMetrics;
        private final Font font;
        private final FontStrike strike;
        private final FontResource resource;
        private final CharToGlyphMapper mapper;
        private final Map<Integer, Float> advanceCache = new ConcurrentHashMap<>();

        FxFontMetrics(Font font) {
            FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
            this.font = Objects.requireNonNull(font);
            this.fontMetrics = fontLoader.getFontMetrics(font);

            var pgFont = (PGFont) FontHelper.getNativeFont(font);
            this.strike = pgFont.getStrike(BaseTransform.IDENTITY_TRANSFORM, FontResource.AA_GREYSCALE);
            this.resource = strike.getFontResource();
            this.mapper  = resource.getGlyphMapper();
        }
        @Override
        public Object getFont() {
            return font;
        }
        @Override
        public final float getAscent() {
            return fontMetrics.getAscent();
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

}
