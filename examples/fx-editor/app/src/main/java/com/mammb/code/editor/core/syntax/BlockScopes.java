/*
 * Copyright 2023-2024 the original author or authors.
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
package com.mammb.code.editor.core.syntax;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public class BlockScopes {

    private final TreeMap<Anchor, Token> scopes = new TreeMap<>();

    public void clear(int row) {
        scopes.subMap(new Anchor(row, 0), new Anchor(row, Integer.MAX_VALUE)).clear();
    }

    public void putOpen(int row, int col, BlockType.Range type) {
        scopes.put(new Anchor(row, col), new StartToken(type));
    }

    public void putClose(int row, int col, BlockType.Range type) {
        scopes.put(new Anchor(row, col), new EndToken(type));
    }

    public void putNeutral(int row, int col, BlockType.Neutral type) {
        scopes.put(new Anchor(row, col), new OpenToken(type));
    }

    public Optional<BlockType> inScope(int row, int col) {
        Deque<Token> neutralStack = new ArrayDeque<>();
        Deque<Token> rangeStack = new ArrayDeque<>();
        for (var e : scopes.headMap(new Anchor(row, col)).entrySet()) {
            switch (e.getValue()) {
                case StartToken t -> rangeStack.push(t);
                case EndToken t -> {
                    if (rangeStack.peek() instanceof StartToken startToken &&
                            Objects.equals(t.type(), startToken.type())) {
                        rangeStack.pop();
                    }
                }
                case OpenToken t  -> {
                    if (!neutralStack.isEmpty() &&
                            Objects.equals(t.type(), neutralStack.peek().type())) {
                        neutralStack.pop();
                    } else {
                        neutralStack.push(t);
                    }
                }
            }
        }
        if (!rangeStack.isEmpty()) {
            return Optional.of(rangeStack.peek().type());
        }
        if (!neutralStack.isEmpty()) {
            return Optional.of(neutralStack.peek().type());
        }
        return Optional.empty();
    }

    record Anchor(int row, int col) implements Comparable<Anchor> {
        @Override
        public int compareTo(Anchor that) {
            int c = Integer.compare(this.row, that.row);
            return c == 0 ? Integer.compare(this.col, that.col) : c;
        }
    }

    private sealed interface Token { BlockType type(); }
    private record OpenToken(BlockType.Neutral type) implements Token { }
    private record StartToken(BlockType.Range type) implements Token { }
    private record EndToken(BlockType.Range type) implements Token { }


    public interface BlockType {

        String open();
        String close();

        interface Neutral extends BlockType {
            default String close() { return open(); }
        }
        interface Range extends BlockType { }

        static Neutral neutral(String open) {
            record NeutralRecord(String open) implements Neutral { }
            return new NeutralRecord(open);
        }
        static Range range(String open, String close) {
            record RangeRecord(String open, String close) implements Range { }
            return new RangeRecord(open, close);
        }
    }


}
