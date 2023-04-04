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
package com.mammb.code.editor3.syntax;

import com.mammb.code.editor3.model.DecoratedText;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cutup.
 * Reduce memory cost for substring operation.
 * @author Naotsugu Kobayashi
 */
public class Cutup {

    /** The ColoringTo. */
    private final ColoringTo coloringTo;

    /** Cutup entry. */
    private final List<Entry> list = new ArrayList<>();


    /**
     * Constructor.
     * @param coloringTo the ColoringTo
     */
    public Cutup(ColoringTo coloringTo) {
        this.coloringTo = coloringTo;
    }


    /**
     * Add cutup plan.
     * @param beginIndex the beginning index, inclusive
     * @param endIndex the ending index, exclusive
     * @param type the type of entry
     */
    public void add(int beginIndex, int endIndex, int type) {
        if (!list.isEmpty() &&
            list.get(list.size() - 1).canMarge(beginIndex, endIndex, type)) {
            list.get(list.size() - 1).marge(beginIndex, endIndex, type);
        } else {
            list.add(new Entry(beginIndex, endIndex, type));
        }
    }


    /**
     * Get the {@link DecoratedText} list by the plan.
     * @param string the source string
     * @return the {@link DecoratedText} list
     */
    public List<DecoratedText> getList(String string) {

        List<DecoratedText> ret = list.stream()
            .map(e -> DecoratedText.of(string.substring(e.beginIndex, e.endIndex), coloringTo.apply(e.type)))
            .collect(Collectors.toList());

        Entry last = list.isEmpty() ? null : list.get(list.size() - 1);
        if (last != null && last.endIndex < string.length()) {
            ret.add(DecoratedText.of(string.substring(last.endIndex), coloringTo.apply(last.type)));
        }
        list.clear();
        return ret;
    }


    /**
     * Cutup entry.
     */
    static class Entry {

        /** The beginning index, inclusive. */
        private final int beginIndex;

        /** The ending index, exclusive. */
        private int endIndex;

        /** The type of entry. */
        private final int type;

        /**
         * Constructor.
         * @param beginIndex the beginning index, inclusive
         * @param endIndex the ending index, exclusive
         * @param type the type of entry
         */
        public Entry(int beginIndex, int endIndex, int type) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.type = type;
        }


        /**
         * Get whether the given conditions can marge.
         * @param beginIndex the beginning index, inclusive
         * @param endIndex the ending index, exclusive
         * @param type the type of entry
         * @return {@code true}, if given conditions can marge
         */
        boolean canMarge(int beginIndex, int endIndex, int type) {
            return this.endIndex == beginIndex && this.type == type;
        }


        /**
         * Marge the other entry to this entry.
         * @param beginIndex the beginning index, inclusive
         * @param endIndex the ending index, exclusive
         * @param type the type of entry
         */
        void marge(int beginIndex, int endIndex, int type) {
            if (!canMarge(beginIndex, endIndex, type)) {
                throw new RuntimeException("Can't marge");
            }
            this.endIndex = endIndex;
        }
    }

}
