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
package com.mammb.code.editor2.model;

/**
 * ScrollBehavior.
 * @author Naotsugu Kobayashi
 */
public interface ScrollBehavior {

    /**
     * Scroll next.
     */
    void next();

    /**
     * Scroll prev.
     */
    void prev();

    void toCaret();


    class NowrapScrollBehavior implements ScrollBehavior {

        private final TextSource source;
        private final TextView textView;

        public NowrapScrollBehavior(TextSource source, TextView textView) {
            this.source = source;
            this.textView = textView;
        }

        @Override
        public void next() {
            String tail = source.afterRow(textView.text().codePointCount());
            source.shiftRow(1);
            textView.text().shiftAppend(tail);
            textView.setOriginRow(textView.originRow() + 1);
        }

        @Override
        public void prev() {
            source.shiftRow(-1);
            String head = source.rows(1);
            textView.text().shiftInsert(0, head);
            textView.setOriginRow(textView.originRow() - 1);
        }

        @Override
        public void toCaret() {

        }
    }

    class WrapScrollBehavior implements ScrollBehavior {

        private final TextSource source;
        private final TextView textView;

        public WrapScrollBehavior(TextSource source, TextView textView) {
            this.source = source;
            this.textView = textView;
        }

        @Override
        public void next() {
        }

        @Override
        public void prev() {
        }

        @Override
        public void toCaret() {

        }
    }
}
