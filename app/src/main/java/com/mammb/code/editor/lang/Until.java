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
package com.mammb.code.editor.lang;

import java.util.function.Predicate;

/**
 * Until utilities.
 * @author Naotsugu Kobayashi
 */
public class Until {

    /**
     * Create a new Until char length.
     * @return the Until char length
     */
    public static Until.CharLen charLength(int length) {
        return new Until.CharLen(length);
    }


    /**
     * Create a new Until LF.
     * @param count the line feed count
     * @return the Until LF predicate
     */
    public static Until.LF lf(int count) {
        return new Until.LF(count, true);
    }


    /**
     * Create a new Until LF.
     * @return the Until LF predicate
     */
    public static Until.LF lf() {
        return new Until.LF(1, true);
    }


    /**
     * Create a new Until LF.
     * @param count the line feed count
     * @return the Until LF predicate
     */
    public static Until.LF lfInclusive(int count) {
        return new Until.LF(count, false);
    }


    /**
     * Create a new Until LF.
     * @return the Until LF predicate
     */
    public static Until.LF lfInclusive() {
        return new Until.LF(1, false);
    }


    /** CharLen. */
    static class CharLen implements Predicate<byte[]> {

        /** The char count. */
        private int count;

        /**
         * Constructor.
         * @param count the line feed count
         */
        public CharLen(int count) {
            this.count = count;
        }

        @Override
        public boolean test(byte[] bytes) {
            count -= Strings.lengthByteAsUtf16(bytes[0]);
            return count < 0;
        }
    }


    /** LF. */
    static class LF implements Predicate<byte[]> {

        /** The line feed count. */
        private int count;

        /** The exclusive flag. */
        private boolean exclusive;

        /**
         * Constructor.
         * @param count the line feed count
         * @param exclusive the exclusive flag
         */
        public LF(int count, boolean exclusive) {
            if (count <= 0) throw new IllegalArgumentException();
            this.count = count;
            this.exclusive = exclusive;
        }

        @Override
        public boolean test(byte[] bytes) {
            if (bytes != null && bytes.length > 0 && bytes[0] == '\n') {
                count--;
            }
            boolean ret = exclusive && count <= 0;
            if (count <= 0) {
                exclusive = true;
            }
            return ret;
        }
    }

}
