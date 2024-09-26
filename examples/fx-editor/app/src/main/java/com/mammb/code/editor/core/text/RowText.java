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
package com.mammb.code.editor.core.text;

import com.mammb.code.editor.core.FontMetrics;

/**
 * The RowText.
 * @author Naotsugu Kobayashi
 */
public interface RowText extends LinedText {

    default int line() {
        return row();
    }

    static RowText of(int row, String text, FontMetrics fm) {
        double width = 0;
        double[] advances = new double[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char ch1 = text.charAt(i);
            if (Character.isHighSurrogate(ch1)) {
                width += advances[i] = fm.getAdvance(ch1, text.charAt(i + 1));
                i++;
            } else if (Character.isISOControl(ch1)) {
                i++;
            } else if (ch1 == '\t') {
                width += advances[i] = fm.getAdvance(" ".repeat(4));
            } else {
                width += advances[i] = fm.getAdvance(ch1);
            }
        }
        return new RowTextRecord(row, text, advances, width, fm.getLineHeight());
    }

    record RowTextRecord(int row, String value, double[] advances, double width, double height)
            implements RowText {
    }

}
