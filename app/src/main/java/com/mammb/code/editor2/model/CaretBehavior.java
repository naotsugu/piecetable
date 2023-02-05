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
 * CaretBehavior.
 * @author Naotsugu Kobayashi
 */
public interface CaretBehavior {
    void next();
    void prev();
    void NextRow();
    void prevRow();
    void home();
    void end();

    class NowrapCaretBehavior implements CaretBehavior {

        private final TextView textView;

        NowrapCaretBehavior(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void next() {
            int nextIndex = textView.caretIndex() + 1;
            if (nextIndex >= textView.text().length()) return;
            if (textView.text().charAt(nextIndex) == '\n') {
                textView.caretPoint().toNextHead();
            } else {
                textView.caretPoint().toNext();
            }
        }

        @Override
        public void prev() {
            int prevIndex = textView.caretIndex() - 1;
            if (prevIndex <= 0) return;
            if (textView.text().charAt(prevIndex) == '\n') {
                textView.caretPoint().toPrevTail(
                    textView.text().rowLength(textView.caretPoint().row() - 1));
            } else {
                textView.caretPoint().toPrev();
            }
        }

        @Override
        public void NextRow() {
            if (textView.caretPoint().row() == textView.rowSize() - 1) return;
            textView.caretPoint().toNextRow();
        }

        @Override
        public void prevRow() {
            if (textView.caretPoint().row() == 0) return;
            textView.caretPoint().toPrevRow();
        }

        @Override
        public void home() {}
        @Override
        public void end() {}
    }

    class WrapCaretBehavior implements CaretBehavior {

        private final TextView textView;

        WrapCaretBehavior(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void next() {

        }

        @Override
        public void prev() {

        }

        @Override
        public void NextRow() {

        }

        @Override
        public void prevRow() {

        }

        @Override
        public void home() {

        }

        @Override
        public void end() {

        }
    }
}
