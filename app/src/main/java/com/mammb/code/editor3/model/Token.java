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
 * Token.
 * @author Naotsugu Kobayashi
 */
public class Token {

    /** The name of token. */
    private String name;
    /** The position. */
    private int position;
    /** The length. */
    private int length;


    private boolean adjoining(Token other) {
        return position + length == other.position;
    }

    public Token marge(Token other) {
        if (!adjoining(other)) {
            throw new IllegalArgumentException(other.toString());
        }
        this.length += other.length;
        return this;
    }

    /**
     * Get the name.
     * @return the name
     */
    public String name() { return name; }

    /**
     * Get the position of this token.
     * @return the position
     */
    public int position() { return position; }

    /**
     * Get the length of this token.
     * @return the length
     */
    public int length() { return length; }

}
