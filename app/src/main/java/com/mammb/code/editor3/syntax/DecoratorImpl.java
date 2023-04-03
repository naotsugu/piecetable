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
        Map<Integer, Deque<Token>> blockScopes = currentScopes();
        Deque<Token> inlineScopes = new ArrayDeque<>();
        lexer.setSource(LexerSource.of(string));

        int prevType = -1;
        int beginIndex = 0;

        Cutup cutup = new Cutup();

        for (Token token = lexer.nextToken(); !token.isEmpty(); token = lexer.nextToken()) {

            if (token.scope().isBlock()) {
                tokens.put(origin.offset() + token.position(), token);
                if (token.scope().isStart()) {
                    blockScopes.computeIfAbsent(token.type(), ArrayDeque::new).push(token);
                } else if (token.scope().isEnd() && blockScopes.containsKey(token.type())) {
                    blockScopes.get(token.type()).poll();
                } else if (token.scope().isAny()) {
                    Deque<Token> deque = blockScopes.get(token.type());
                    if (deque == null || deque.isEmpty()) {
                        blockScopes.computeIfAbsent(token.type(), ArrayDeque::new).push(token);
                    } else {
                        blockScopes.get(token.type()).poll();
                    }
                }

            } else if (token.scope().isInline()) {
                if (token.scope().isStart()) {
                    inlineScopes.push(token);
                } else if (token.scope().isEnd()) {
                    inlineScopes.clear();
                }
            }

            Optional<Token> currentScope = blockScopes.values().stream()
                .flatMap(Collection::stream)
                .min(Comparator.comparing(Token::type))
                .or(() -> Optional.ofNullable(inlineScopes.peek()));

            if (currentScope.isPresent()) {
                if (prevType < 0) {
                    prevType = currentScope.get().type();
                    beginIndex = token.position();
                } else if (prevType != currentScope.get().type()) {
                    cutup.add(beginIndex, token.position(), prevType);
                    prevType = currentScope.get().type();
                    beginIndex = token.position();
                }
                continue;
            } else if (prevType > 0) {
                cutup.add(beginIndex, token.position(), prevType);
                prevType = -1;
            }

            cutup.add(token.position(), token.position() + token.length(), token.type());
        }

        return cutup.getList(string);
    }


    /**
     * Get the scope map.
     * key: token type
     * @return the scope map
     */
    private Map<Integer, Deque<Token>> currentScopes() {
        Map<Integer, Deque<Token>> scopes = new HashMap<>();
        for (Map.Entry<Integer, Token> entry : tokens.entrySet()) {
            Token token = entry.getValue();
            if (!token.scope().isBlock()) {
                throw new IllegalStateException(token.scope().toString());
            }
            if (token.scope().isStart()) {
                scopes.computeIfAbsent(token.type(), ArrayDeque::new).push(token);
            } else if (token.scope().isEnd() && scopes.containsKey(token.type())) {
                scopes.get(token.type()).poll();
            } else if (token.scope().isAny()) {
                // Toggle scope if any
                Deque<Token> deque = scopes.get(token.type());
                if (deque == null || deque.isEmpty()) {
                    scopes.computeIfAbsent(token.type(), ArrayDeque::new).push(token);
                } else {
                    scopes.get(token.type()).poll();
                }
            }
        }
        return scopes;
    }

}
