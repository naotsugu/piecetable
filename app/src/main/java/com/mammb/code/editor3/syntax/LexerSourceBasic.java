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

/**
 * LexerSourceBasic.
 * @author Naotsugu Kobayashi
 */
public class LexerSourceBasic implements LexerSource {

    /** The input string. */
    private String string;

    /** The current position in input (points to current char). */
    private int position = -1;

    /** The peeked count. */
    private int peekCount = 0;


    /**
     * Constructor.
     * @param string the source string
     */
    private LexerSourceBasic(String string) {
        this.string = string;
    }


    /**
     * Create a new LexerSource.
     * @param string the source string
     * @return a LexerSource
     */
    public static LexerSource of(String string) {
        return new LexerSourceBasic(string);
    }


    @Override
    public char readChar() {
        position++;
        peekCount = 0;
        if (position >= string.length()) {
            position = string.length();
            return 0;
        }
        return string.charAt(position);
    }


    @Override
    public char currentChar() {
        if (position < 0 || position >= string.length()) {
            return 0;
        }
        return string.charAt(position);
    }


    @Override
    public char peekChar() {
        peekCount++;
        int index = position + peekCount;
        if (index >= string.length()) {
            peekCount = Math.min(string.length() - position, peekCount);
            return 0;
        }
        return string.charAt(index);
    }


    @Override
    public void commitPeek() {
        position += peekCount;
        peekCount = 0;
    }


    @Override
    public void rollbackPeek() {
        peekCount = 0;
    }


    @Override
    public int position() {
        return position;
    }


    @Override
    public int length() {
        return string.length();
    }

}
