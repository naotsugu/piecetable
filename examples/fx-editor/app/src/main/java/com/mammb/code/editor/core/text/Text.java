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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Text.
 * @author Naotsugu Kobayashi
 */
public interface Text {

    int row();

    String value();

    double[] advances();

    double width();

    double height();

    default int length() {
        return value().length();
    }

    default int textLength() {
        int len = value().length();
        char ch1 = (len > 0) ? value().charAt(len - 1) : 0;
        char ch2 = (len > 1) ? value().charAt(len - 2) : 0;
        return len - (
                (ch2 == '\r' && ch1  == '\n') ? 2 : (ch1  == '\n') ? 1 : 0
        );
    }

    default boolean isSurrogate(int index) {
        return Character.isSurrogate(value().charAt(index));
    }

    default boolean isHighSurrogate(int index) {
        return Character.isHighSurrogate(value().charAt(index));
    }

    default boolean isLowSurrogate(int index) {
        return Character.isLowSurrogate(value().charAt(index));
    }

    default int indexRight(int index) {
        if (isEmpty()) return index;
        index += isHighSurrogate(index) ? 2 : 1;
        return (index > textLength()) ? -1 : index;
    }

    default int indexLeft(int index) {
        if (index <= 0) return 0;
        index -= isLowSurrogate(index - 1) ? 2 : 1;
        return index;
    }

    default double widthTo(int index) {
        double[] ad = advances();
        return Arrays.stream(ad, 0, Math.min(index, ad.length)).sum();
    }

    default int indexTo(double width) {
        double[] ad = advances();
        double w = 0;
        for (int i = 0; i < ad.length; i++) {
            if (w + ad[i] > width) return i;
            w += ad[i];
        }
        return Math.min(ad.length, textLength());
    }

    default boolean isEmpty() {
        return value().isEmpty();
    }

    default List<Text> words() {
        List<Text> ret = new ArrayList<>();
        var text = value();
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next();
             end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            ret.add(Text.of(
                    row(),
                    text.substring(start, end),
                    Arrays.copyOfRange(advances(), start, end),
                    height()));
        }
        return ret;
    }

    static Text of(int row, String value, double[] advances, double height) {
        record TextRecord(int row, String value, double[] advances, double width, double height) implements Text { }
        return new TextRecord(row, value, advances, Arrays.stream(advances).sum(), height);
    }

}
