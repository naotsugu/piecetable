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
 * <pre>
 *  index  target          bufIndex   length
 *     0 | read buffer   |  0        | 3     |   PiecePoint(0, 0)
 *     1 | append buffer |  0        | 2     |   PiecePoint(1, 3)
 *     2 | append buffer |  5        | 6     |   PiecePoint(2, 5)
 * </pre>
 * @author Naotsugu Kobayashi
 */
public class PiecePoint {

    /** The index of the piece. */
    private int index;

    /** The position on the head of piece. */
    private long position;


    /**
     * Constructor.
     * @param index the index of the piece
     * @param position the position on the head of piece
     */
    private PiecePoint(int index, long position) {
        this.index = index;
        this.position = position;
    }


    /**
     * Create a new {@code PiecePoint}.
     */
    public PiecePoint() {
        this(0, 0);
    }


    /**
     * Get the piece point copy.
     * @return the copied piece point
     */
    public PiecePoint copy() {
        return new PiecePoint(index, position);
    }


    /**
     * Increment this point.
     * @param length the length
     */
    public void inc(long length) {
        index++;
        position += length;
    }


    /**
     * Decrement this point
     * @param length the length
     */
    public void dec(long length) {
        index--;
        position -= length;
    }


    /**
     * Get the index of the piece.
     * @return the index of the piece
     */
    public int index() {
        return index;
    }


    /**
     * Get the position on the head of piece.
     * @return the position on the head of piece
     */
    public long position() {
        return position;
    }


    @Override
    public String toString() {
        return "PiecePoint{bufIndex=%d, position=%d}".formatted(index, position);
    }

}
