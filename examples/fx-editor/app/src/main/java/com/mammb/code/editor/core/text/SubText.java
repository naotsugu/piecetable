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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The sub text.
 * @author Naotsugu Kobayashi
 */
public interface SubText extends Text {
    int fromIndex();
    int toIndex();
    SubText prev();
    SubText next();
    default boolean hasPrev() {
        return prev() != null;
    }
    default boolean hasNext() {
        return next() != null;
    }
    default boolean isHead() {
        return prev() == null;
    }
    default boolean isTail() {
        return next() == null;
    }
    default boolean contains(int col) {
        return fromIndex() <= col && col < toIndex();
    }

    static List<SubText> of(RowText rowText, double width) {
        if (width <= 0) {
            return List.of(new SubTextRecord(rowText, 0, rowText.length(), rowText.width(), null));
        }
        double w = 0;
        int fromIndex = 0;
        SubText prev = null;
        List<SubText> subs = new ArrayList<>();
        double[] advances = rowText.advances();
        for (int i = 0; i < rowText.length(); i++) {
            double advance = advances[i];
            if (advance <= 0) continue;
            if (w + advance > width) {
                var sub = new SubTextRecord(rowText, fromIndex, i, w, prev);
                if (subs.getLast() != null) {
                    ((SubTextRecord) subs.getLast()).next = sub;
                }
                subs.add(sub);
                prev = sub;
                w = 0;
                fromIndex = i;
            }
            w += advance;
        }
        var sub = new SubTextRecord(rowText, fromIndex, rowText.length(), w, prev);
        if (subs.getLast() != null) {
            ((SubTextRecord) subs.getLast()).next = sub;
        }
        subs.add(sub);
        return subs;
    }

    class SubTextRecord implements SubText {
        private final RowText parent;
        private final int fromIndex;
        private final int toIndex;
        private final double width;
        private final SubText prev;
        private SubText next;

        public SubTextRecord(RowText parent, int fromIndex, int toIndex, double width, SubText prev) {
            this.parent = parent;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.width = width;
            this.prev = prev;
        }

        @Override
        public int row() {
            return parent.row();
        }

        @Override
        public String value() {
            return parent.value().substring(fromIndex, toIndex);
        }

        @Override
        public double[] advances() {
            return Arrays.copyOfRange(parent.advances(), fromIndex, toIndex);
        }

        @Override
        public double width() {
            return width;
        }

        @Override
        public double height() {
            return parent.height();
        }

        @Override
        public int fromIndex() {
            return fromIndex;
        }

        @Override
        public int toIndex() {
            return toIndex;
        }

        @Override
        public SubText prev() {
            return prev;
        }

        @Override
        public SubText next() {
            return next;
        }
    }
}
