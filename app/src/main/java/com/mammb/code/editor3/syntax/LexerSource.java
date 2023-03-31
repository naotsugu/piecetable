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
 * LexerSource.
 * @author Naotsugu Kobayashi
 */
public interface LexerSource {

    /**
     * Reads a character.
     * This operation moves the read position forward.
     * @return a character read. {@code 0} if the read target does not exist
     */
    char readChar();

    /**
     * Get a current character.
     * @return a character read. {@code 0} if the read target does not exist
     */
    char currentChar();

    /**
     * Reads a character.
     * This operation does not change the read position.
     * @return a character read. {@code 0} if the read target does not exist
     */
    char peekChar();

    /**
     * Forward the read position to the peeked position.
     */
    void commitPeek();

    /**
     * Rollback the peeked position.
     */
    void rollbackPeek();

    /**
     * Gets the current read position.
     * @return {@code -1} if loading has not started.
     */
    int position();

    /**
     * Gets the length.
     * @return the length
     */
    int length();

    /**
     * Create a new LexerSource.
     * @param string the source string
     * @return a new LexerSource
     */
    static LexerSource of(String string) {
        return LexerSourceBasic.of(string);
    }

}
