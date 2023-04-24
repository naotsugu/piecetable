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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * LexicalScope.
 * @author Naotsugu Kobayashi
 */
public class LexicalScope {

    /** The block scopes(key:token type number). */
    private final Map<Integer, Deque<Token>> blockScopes = new HashMap<>();

    /** The inline scopes(key:token type number). */
    private final Map<Integer, Deque<Token>> inlineScopes = new HashMap<>();


    /**
     * Create a new LexicalScope.
     * @param entries the token entries
     * @return a new LexicalScope
     */
    public static LexicalScope of(Collection<Map.Entry<Integer, Token>> entries) {
        LexicalScope lexicalScope = new LexicalScope();
        lexicalScope.put(entries);
        return lexicalScope;
    }


    /**
     * Put the tokens.
     * @param entries the tokens
     */
    public void put(Collection<Map.Entry<Integer, Token>> entries) {
        for (Map.Entry<Integer, Token> entry : entries) {
            put(entry.getValue());
        }
    }


    /**
     * Put the token.
     * @param token the token
     */
    public void put(Token token) {
        if (token.scope().isBlock()) {
            putScope(token, blockScopes);
        } else if (token.scope().isInline() &&
                (token.scope().isStart() || token.scope().isEnd())) {
            putScope(token, inlineScopes);
        }
    }


    /**
     * Get the current scope token.
     * @return the current scope token
     */
    public Optional<Token> current() {

        Optional<Token> blockScope = blockScopes.values().stream()
            .flatMap(Collection::stream)
            .min(Comparator.comparing(Token::type));
        if (blockScope.isPresent()) {
            return blockScope;
        }

        return inlineScopes.values().stream()
            .flatMap(Collection::stream)
            .min(Comparator.comparing(Token::type));
    }


    /**
     * Put the scope.
     * @param token the token
     * @param scopes the target scope
     */
    private void putScope(Token token, Map<Integer, Deque<Token>> scopes) {

        if (token.scope().isStart()) {
            scopes.computeIfAbsent(token.type(), ArrayDeque::new)
                .push(token);

        } else if (token.scope().isEnd() && scopes.containsKey(token.type())) {
            scopes.get(token.type())
                .poll();

        } else if (token.scope().isAny()) {
            // toggle scope if any
            Deque<Token> deque = scopes.get(token.type());
            if (deque == null || deque.isEmpty()) {
                scopes.computeIfAbsent(token.type(), ArrayDeque::new).push(token);
            } else {
                scopes.get(token.type()).poll();
            }
        }
    }

}
