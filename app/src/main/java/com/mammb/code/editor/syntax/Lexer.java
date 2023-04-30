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

import com.mammb.code.editor.syntax.type.JavaLexer;
import com.mammb.code.editor.syntax.type.JsonLexer;
import com.mammb.code.editor.syntax.type.MarkdownLexer;
import com.mammb.code.editor.syntax.type.PassThroughLexer;
import com.mammb.code.editor.syntax.type.RustLexer;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public interface Lexer {

    /**
     * Get the name.
     * @return the name
     */
    String name();

    /**
     * Gets the next token.
     * @return the next token
     */
    Token nextToken();


    /**
     * Set the source.
     * @param source the source.
     * @param lexicalScope the lexicalScope.
     */
    void setSource(LexerSource source, LexicalScope lexicalScope);


    /**
     * Get the lexer according to the name.
     * @param name the name
     * @return the lexer
     */
    static Lexer of(String name) {
        return switch (name) {
            case "java" -> JavaLexer.of(name);
            case "json" -> JsonLexer.of(name);
            case "rust" -> RustLexer.of(name);
            case "markdown" -> MarkdownLexer.of(name);
            default -> PassThroughLexer.of(name);
        };
    }


    /**
     * Get the lexer name according to the extension.
     * @param ext the extension
     * @return the lexer name
     */
    static String name(String ext) {
        return switch (ext) {
            case "java" -> "java";
            case "json" -> "json";
            case "rs", "rust" -> "rust";
            case "md" -> "markdown";
            default -> "pt";
        };
    }

}
