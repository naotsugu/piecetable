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
package com.mammb.code.piecetable.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link Piece}.
 * @author Naotsugu Kobayashi
 */
class PieceTest {

    @Test
    void split() {
        var buffer = Buffer.of("012345".getBytes());
        // | 1 | 2 | 3 | 4 | 5 |
        var p1 = new Piece(buffer, 1, 5);

        // | 1 | 2 | 3 | 4 | 5 |
        var pair = p1.split(0);
        assertEquals(new Piece(buffer, 1, 5), pair[0]);

        // | 1 |
        //     | 2 | 3 | 4 | 5 |
        pair = p1.split(1);
        assertEquals(new Piece(buffer, 1, 1), pair[0]);
        assertEquals(new Piece(buffer, 2, 4), pair[1]);

        // | 1 | 2 |
        //         | 3 | 4 | 5 |
        pair = p1.split(2);
        assertEquals(new Piece(buffer, 1, 2), pair[0]);
        assertEquals(new Piece(buffer, 3, 3), pair[1]);

        // | 1 | 2 | 3 |
        //             | 4 | 5 |
        pair = p1.split(3);
        assertEquals(new Piece(buffer, 1, 3), pair[0]);
        assertEquals(new Piece(buffer, 4, 2), pair[1]);

        // | 1 | 2 | 3 | 4 |
        //                 | 5 |
        pair = p1.split(4);
        assertEquals(new Piece(buffer, 1, 4), pair[0]);
        assertEquals(new Piece(buffer, 5, 1), pair[1]);

        // | 1 | 2 | 3 | 4 | 5 |
        pair = p1.split(5);
        assertEquals(new Piece(buffer, 1, 5), pair[0]);

    }

    @Test
    void bytes() {
        var buffer = Buffer.of("012345".getBytes());
        var p1 = new Piece(buffer, 1, 4);
        assertEquals("1234", new String(p1.bytes()));
    }

}
