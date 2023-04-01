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

import com.mammb.code.editor3.model.DecoratedText;
import com.mammb.code.editor3.model.Decorator;
import com.mammb.code.editor3.model.RowPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator.
 * @author Naotsugu Kobayashi
 */
public class DecoratorImpl implements Decorator {

    /** The lexer .*/
    private final Lexer lexer;


    /**
     * Constructor.
     * @param lexer the lexer
     */
    private DecoratorImpl(Lexer lexer) {
        this.lexer = lexer;
    }


    /**
     * Create a new Decorator.
     * @param ext the extension name
     * @return a new Decorator
     */
    public static Decorator of(String ext) {
        return new DecoratorImpl(Lexer.of(ext));
    }


    @Override
    public List<DecoratedText> apply(RowPoint origin, String string) {

        lexer.setSource(LexerSource.of(string));

        List<DecoratedText> list = new ArrayList<>();

        for (Token token = lexer.nextToken(); !token.isEmpty(); token = lexer.nextToken()) {
            if (token.length() == 0) break;

            int pos = token.position();
            String str = string.substring(pos, pos + token.length());

            DecoratedText text;
            if (token.type() == TokenType.KEYWORD.ordinal()) {
                text = DecoratedText.of(str, 1);
            } else {
                text = DecoratedText.of(str);
            }

            list.add(text);
        }

        return list;
    }

}
