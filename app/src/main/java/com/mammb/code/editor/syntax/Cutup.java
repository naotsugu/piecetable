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
package com.mammb.code.editor.syntax;

import com.mammb.code.editor.model.DecoratedText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Cutup.
 * Reduce memory cost for substring operation.
 * @author Naotsugu Kobayashi
 */
public class Cutup {

    /** The ColoringTo. */
    private final DecorateTo decorateTo;

    /** The decorated text list. */
    private final List<DecoratedText> list = new ArrayList<>();

    /** current entry. */
    private Entry entry;


    /**
     * Constructor.
     * @param decorateTo the ColoringTo
     */
    public Cutup(DecorateTo decorateTo) {
        this.decorateTo = Objects.isNull(decorateTo) ? DecorateTo.empty : decorateTo;
    }


    /**
     * Add cutup plan.
     * @param beginIndex the beginning index, inclusive
     * @param endIndex the ending index, exclusive
     * @param type the type of entry
     * @param string the source string
     */
    public void add(int beginIndex, int endIndex, int type, String string) {
        if (entry == null) {
            entry = new Entry(beginIndex, endIndex, type);
        } else if (entry.canMarge(beginIndex, endIndex, type)) {
            entry.marge(beginIndex, endIndex, type);
        } else {
            list.add(DecoratedText.of(
                string.substring(entry.beginIndex, entry.endIndex),
                decorateTo.apply(entry.type)));
            entry = new Entry(beginIndex, endIndex, type);
        }
    }


    /**
     * Get the {@link DecoratedText} list by the plan.
     * @param string the source string
     * @return the {@link DecoratedText} list
     */
    public List<DecoratedText> getList(String string) {
        if (entry != null) {
            list.add(DecoratedText.of(
                string.substring(entry.beginIndex, entry.endIndex),
                decorateTo.apply(entry.type)));
        }
        return list;
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
