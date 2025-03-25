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

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link PieceTableImpl}.
 * @author Naotsugu Kobayashi
 */
class PieceTableImplTest {

    @Test
    void edit() {

        var pt = PieceTableImpl.of();

        pt.insert(0, "ac".getBytes());
        pt.insert(1, "b".getBytes());
        assertEquals("abc", new String(pt.bytes()));

        pt.delete(1, 1);
        assertEquals("ac", new String(pt.bytes()));

        pt.delete(1, 1);
        assertEquals("a", new String(pt.bytes()));

        pt.delete(0, 1);
        assertEquals("", new String(pt.bytes()));
    }

    @Test
    void insert() {

        var pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(0, "123".getBytes());
        assertEquals("123abc", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(1, "123".getBytes());
        assertEquals("a123bc", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(2, "123".getBytes());
        assertEquals("ab123c", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "123".getBytes());
        assertEquals("abc123", new String(pt.bytes()));
    }

    @Test
    void deleteWithSinglePiece() {

        var pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.delete(0, 1);
        assertEquals("bc", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.delete(1, 1);
        assertEquals("ac", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.delete(2, 1);
        assertEquals("ab", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.delete(0, 2);
        assertEquals("c", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.delete(1, 2);
        assertEquals("a", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.delete(0, 3);
        assertEquals("def", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.delete(3, 3);
        assertEquals("abc", new String(pt.bytes()));
    }

    @Test
    void deleteWithSomePiece() {

        var pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.delete(0, 4);
        assertEquals("ef", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.delete(1, 4);
        assertEquals("af", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.insert(6, "ghi".getBytes());
        pt.delete(0, 5);
        assertEquals("fghi", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.insert(6, "ghi".getBytes());
        pt.delete(1, 5);
        assertEquals("aghi", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.insert(6, "ghi".getBytes());
        pt.delete(2, 5);
        assertEquals("abhi", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.insert(6, "ghi".getBytes());
        pt.delete(3, 5);
        assertEquals("abci", new String(pt.bytes()));

        pt = PieceTableImpl.of();
        pt.insert(0, "abc".getBytes());
        pt.insert(3, "def".getBytes());
        pt.insert(6, "ghi".getBytes());
        pt.delete(4, 5);
        assertEquals("abcd", new String(pt.bytes()));
    }

    @Test
    void get() {

        var pt = PieceTableImpl.of();
        pt.insert(0, "ab".getBytes());
        pt.insert(2, "cd".getBytes());
        pt.insert(4, "ef".getBytes());

        assertEquals("abcdef", new String(pt.get(0, 6)));
        assertEquals("bcdef", new String(pt.get(1, 5)));
        assertEquals("cde", new String(pt.get(2, 3)));
        assertEquals("def", new String(pt.get(3, 3)));
        assertEquals("abcde", new String(pt.get(0, 5)));
    }

}
