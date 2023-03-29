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

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public class Lexer {

    /** The input string. */
    private InputStream input;

    /** The current position in input (points to current char). */
    private int position = 0;


    private Lexer(InputStream input) {
        this.input = input.markSupported() ? input : new BufferedInputStream(input);
    }

    public static Lexer of(InputStream input) {
        return new Lexer(input);
    }

}
