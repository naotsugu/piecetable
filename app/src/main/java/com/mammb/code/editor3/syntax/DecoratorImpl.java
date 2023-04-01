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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return new DecoratorImpl(Lexer.of(ext));
    }


    @Override
    public List<DecoratedText> apply(RowPoint origin, String string) {

        tokens.subMap(origin.offset(), Integer.MAX_VALUE).clear();
        Map<Integer, Deque<Token>> scopes = currentScopes();
        lexer.setSource(LexerSource.of(string));

        int prevType = -1;
        int beginIndex = 0;

        List<DecoratedText> list = new ArrayList<>();

        for (Token token = lexer.nextToken(); !token.isEmpty(); token = lexer.nextToken()) {

            if (token.scope().isBlock()) {
                tokens.put(origin.offset() + token.position(), token);
            }

            if (token.scope().isStart()) {
                scopes.computeIfAbsent(token.type(), ArrayDeque::new).push(token);
            } else if (token.scope().isEnd()) {
                scopes.getOrDefault(token.type(), new ArrayDeque<>()).poll();
            }

            Optional<Token> currentScope = scopes.values().stream()
                .flatMap(Collection::stream)
                .min(Comparator.comparing(Token::type));

            if (currentScope.isPresent()) {
                if (prevType < 0) {
                    prevType = currentScope.get().type();
                    beginIndex = token.position();
                } else if (prevType != currentScope.get().type()) {
                    list.add(DecoratedText.of(
                        string.substring(beginIndex, token.position() + token.length() - 1),
                        prevType));
                    prevType = currentScope.get().type();
                    beginIndex = token.position();
                }
                continue;
            } else if (prevType > 0) {
                list.add(DecoratedText.of(
                    string.substring(beginIndex, token.position() + token.length() - 1),
                    prevType));
                prevType = -1;
            }

            list.add(DecoratedText.of(
                string.substring(token.position(), token.position() + token.length()),
                token.type()));
        }

        return list;
    }


    private Map<Integer, Deque<Token>> currentScopes() {
        Map<Integer, Deque<Token>> scopes = new HashMap<>();
        for (Map.Entry<Integer, Token> entry : tokens.entrySet()) {
            if (entry.getValue().scope().isStart()) {
                scopes.computeIfAbsent(entry.getKey(), ArrayDeque::new).push(entry.getValue());
            } else if (entry.getValue().scope().isEnd()) {
                scopes.getOrDefault(entry.getKey(), new ArrayDeque<>()).poll();
            }
        }
        return scopes;
    }

}
