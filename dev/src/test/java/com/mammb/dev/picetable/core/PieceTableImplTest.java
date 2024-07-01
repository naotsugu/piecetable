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
package com.mammb.dev.picetable.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link PieceTableImplTest}.
 * @author Naotsugu Kobayashi
 */
class PieceTableImplTest {

    @Test
    void edit() {

        var pieceTable = PieceTableImpl.of();

        pieceTable.insert(0, "ac".getBytes());
        pieceTable.insert(1, "b".getBytes());
        assertEquals("abc", new String(pieceTable.bytes()));

        pieceTable.delete(1, 1);
        assertEquals("ac", new String(pieceTable.bytes()));

        pieceTable.delete(1, 1);
        assertEquals("a", new String(pieceTable.bytes()));

        pieceTable.delete(0, 1);
        assertEquals("", new String(pieceTable.bytes()));
    }


    @Test
    void get() {

        var pieceTable = PieceTableImpl.of();
        pieceTable.insert(0, "ab".getBytes());
        pieceTable.insert(2, "cd".getBytes());
        pieceTable.insert(4, "ef".getBytes());

        assertEquals("abcdef", new String(pieceTable.get(0, 6)));
        assertEquals("bcdef", new String(pieceTable.get(1, 5)));
        assertEquals("abcde", new String(pieceTable.get(0, 5)));


    }

}
