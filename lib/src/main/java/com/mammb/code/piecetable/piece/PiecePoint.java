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
package com.mammb.code.piecetable.piece;

/**
 * Pieces index with piece start position.
 * @author Naotsugu Kobayashi
 */
public class PiecePoint {

    private int index;
    private int position;

    private PiecePoint(int index, int position) {
        this.index = index;
        this.position = position;
    }

    public PiecePoint() {
        this(0, 0);
    }

    public PiecePoint copy() {
        return new PiecePoint(index, position);
    }

    public void inc(int length) {
        index++;
        position += length;
    }

    public void dec(int length) {
        index--;
        position -= length;
    }

    public int index() {
        return index;
    }

    public int position() {
        return position;
    }

    @Override
    public String toString() {
        return "PiecePoint{bufIndex=%d, position=%d}".formatted(index, position);
    }
}
