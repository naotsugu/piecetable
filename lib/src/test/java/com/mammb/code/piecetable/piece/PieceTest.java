/*
 * Copyright 2022-2024 the original author or authors.
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

import com.mammb.code.piecetable.buffer.Buffers;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link Piece}.
 * @author Naotsugu Kobayashi
 */
class PieceTest {

    @Test
    void split() {
        var buffer = Buffers.appendOf();
        // | 1 | 2 | 3 | 4 | 5 |
        var p1 = new Piece(buffer, 1, 5);

        // | 1 |
        //     | 2 | 3 | 4 | 5 |
        var pair = p1.split(1);
        assertEquals(new Piece(buffer, 1, 1), pair.left());
        assertEquals(new Piece(buffer, 2, 4), pair.right());

        // | 1 | 2 |
        //         | 3 | 4 | 5 |
        pair = p1.split(2);
        assertEquals(new Piece(buffer, 1, 2), pair.left());
        assertEquals(new Piece(buffer, 3, 3), pair.right());

        // | 1 | 2 | 3 |
        //             | 4 | 5 |
        pair = p1.split(3);
        assertEquals(new Piece(buffer, 1, 3), pair.left());
        assertEquals(new Piece(buffer, 4, 2), pair.right());

        // | 1 | 2 | 3 | 4 |
        //                 | 5 |
        pair = p1.split(4);
        assertEquals(new Piece(buffer, 1, 4), pair.left());
        assertEquals(new Piece(buffer, 5, 1), pair.right());
    }


    @Test
    void marge() {
        var buffer = Buffers.appendOf();

        // | 0 | 1 |
        //         | 2 | 3 |
        var p1 = new Piece(buffer, 0, 2);
        var p2 = new Piece(buffer, 2, 2);

        assertEquals(new Piece(buffer, 0, 4), p1.marge(p2).get());
        assertEquals(new Piece(buffer, 0, 4), p2.marge(p1).get());

        // | 0 | 1 |
        //               | 3 | 4 |
        p1 = new Piece(buffer, 0, 2);
        p2 = new Piece(buffer, 3, 2);

        assertEquals(Optional.empty(), p1.marge(p2));
        assertEquals(Optional.empty(), p2.marge(p1));

    }

}
