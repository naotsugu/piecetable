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

import com.mammb.code.editor3.syntax.type.JavaLexer;
import com.mammb.code.editor3.syntax.type.JsonLexer;
import com.mammb.code.editor3.syntax.type.MarkdownLexer;
import com.mammb.code.editor3.syntax.type.PassThroughLexer;
import com.mammb.code.editor3.syntax.type.RustLexer;

/**
 * Lexer.
 * @author Naotsugu Kobayashi
 */
public interface Lexer {

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
     * Get the lexer according to the extension.
     * @param ext the extension
     * @return the lexer
     */
    static Lexer of(String ext) {
        return switch (ext) {
            case "java" -> JavaLexer.of();
            case "json" -> JsonLexer.of();
            case "rs", "rust" -> RustLexer.of();
            case "md" -> MarkdownLexer.of();
            default -> PassThroughLexer.of();
        };
    }

}
