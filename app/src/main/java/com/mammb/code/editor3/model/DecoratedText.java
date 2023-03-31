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
package com.mammb.code.editor3.model;

/**
 * DecoratedText.
 * @author Naotsugu Kobayashi
 */
public interface DecoratedText {

    String text();

    int size();

    int color();

    boolean bold();

    boolean underLine();

    boolean italic();

    default boolean normal() {
        return !bold() && !underLine() && !italic();
    }

    static DecoratedText of(String text) {
        return new DecoratedText() {
            @Override
            public String text() { return text; }

            @Override
            public int size() { return 16; }

            @Override
            public int color() { return 0; }

            @Override
            public boolean bold() { return false; }

            @Override
            public boolean underLine() { return false; }

            @Override
            public boolean italic() { return false; }
        };
    }

}


