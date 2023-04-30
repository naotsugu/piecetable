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

import com.mammb.code.editor.model.DecoratedText;
import com.mammb.code.editor.model.Decorator;
import com.mammb.code.editor.model.RowPoint;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Decorator.
 * @author Naotsugu Kobayashi
 */
public class DecoratorImpl implements Decorator {

    /** The lexer .*/
    private final Lexer lexer;

    /** The block tokens .*/
    private final TreeMap<Integer, Token> tokens = new TreeMap<>();


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
        return new DecoratorImpl(Lexer.of(Lexer.name(ext)));
    }


    @Override
    public List<DecoratedText> apply(RowPoint origin, String string) {

        tokens.subMap(origin.offset(), Integer.MAX_VALUE).clear();
        LexicalScope scope = LexicalScope.of(tokens.entrySet());
        lexer.setSource(LexerSource.of(string), scope);
        Cutup cutup = new Cutup((lexer instanceof DecorateTo c) ? c : null);

        int prevType = -1, beginIndex = 0;
        for (Token token = lexer.nextToken();
             !token.isEmpty();
             token = lexer.nextToken()) {

            if (token.scope().isBlock() || token.scope().isContext()) {
                tokens.put(origin.offset() + token.position(), token);
            }
            if (!token.scope().isNeutral()) {
                scope.put(token);
            }

            Optional<Token> currentScope = scope.current();
            if (currentScope.isPresent()) {
                if (prevType < 0) {
                    prevType = currentScope.get().type();
                    beginIndex = token.position();
                } else if (prevType != currentScope.get().type()) {
                    cutup.add(beginIndex, token.position(), prevType, string);
                    prevType = currentScope.get().type();
                    beginIndex = token.position();
                }
                continue;
            } else if (prevType > 0) {
                cutup.add(beginIndex, token.position(), prevType, string);
                prevType = -1;
            }
            cutup.add(token.position(), token.position() + token.length(), token.type(), string);
        }

        if (prevType > 0) {
            cutup.add(beginIndex, string.length(), prevType, string);
        }

        return cutup.getList(string);
    }

}
