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
package com.mammb.code.editor.core.syntax;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class LexerSource {
    private int row;
    private String text;
    private int index = 0;
    private int peek = 0;

    private LexerSource(int row, String text) {
        this.row = row;
        this.text = text;
    }

    public static LexerSource of(int row, String source) {
        return new LexerSource(row, source);
    }

    public int row() { return row; }
    public String text() { return text; }
    public int length() { return text.length(); }

    public boolean hasNext() {
        return index < text.length();
    }

    public boolean match(char ch) {
        return text.charAt(index) == ch;
    }

    public boolean match(CharSequence cs) {
        return index + cs.length() < text.length() &&
                Objects.equals(text.substring(index, index + cs.length()), cs.toString());
    }

    public Indexed next() {
        var ret = new Indexed(index, text.charAt(index), text.length());
        index++;
        peek = 0;
        return ret;
    }

    public Indexed next(int n) {
        var ret = new Indexed(index, text.substring(index, index + n), text.length());
        index += n;
        peek = 0;
        return ret;
    }

    public Indexed nextRemaining() {
        var ret = new Indexed(index, text.substring(index), text.length());
        index = text.length();
        peek = 0;
        return ret;
    }

    public Optional<Indexed> nextMatch(String until) {
        int n = text.substring(index).indexOf(until);
        if (n < 0) {
            index = text.length();
            peek = 0;
            return Optional.empty();
        }
        var ret = new Indexed(index + n, until, text.length());
        index = ret.index + until.length();
        peek = 0;
        return Optional.of(ret);
    }

    public Indexed peek() {
        var ret = new Indexed(index + peek, text.charAt(index + peek), text.length());
        peek++;
        return ret;
    }

    public Indexed nextAlphabetic() {
        return nextUntil(Character::isAlphabetic);
    }

    public Indexed nextIdentifierPart() {
        return nextUntil(Character::isUnicodeIdentifierPart);
    }

    public Indexed nextUntil(Predicate<Character> predicate) {
        int i = index;
        for (; i < text.length(); i++) {
            if (!predicate.test(text.charAt(i))) break;
        }
        var ret = new Indexed(index, text.substring(index, i), text.length());
        index = i;
        peek = 0;
        return ret;
    }

    public LexerSource rollbackPeek() {
        peek = 0;
        return this;
    }

    public LexerSource commitPeek() {
        index += peek;
        peek = 0;
        return this;
    }

    public record Indexed(int index, String string, int parentLength) {
        private Indexed(int index, char ch, int parentLength) {
            this(index, String.valueOf(ch), parentLength);
        }
        char ch() {
            return length() == 0 ? 0 : string.charAt(0);
        }
        int lastIndex() {
            return index + string.length() - 1;
        }
        int length() { return string.length(); }
        boolean isFirst() { return index == 0; }
        boolean isLast() { return index == parentLength - 1; }
    }

}
