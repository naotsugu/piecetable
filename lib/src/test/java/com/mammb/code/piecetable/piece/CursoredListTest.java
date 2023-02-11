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

import com.mammb.code.piecetable.buffer.Buffer;
import com.mammb.code.piecetable.buffer.ReadBuffer;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link CursoredList}.
 * @author Naotsugu Kobayashi
 */
class CursoredListTest {

    Charset cs = StandardCharsets.UTF_8;

    @Test
    void testCursorList() {

        var list = new CursoredList();

        list.add(0, new Piece(null, 0, 3));
        // |0|1|2|
        assertEquals(1, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(0, list.at(0).index());
        assertEquals(0, list.at(0).position());
        assertEquals(0, list.at(2).index());
        assertEquals(0, list.at(2).position());


        list.add(1, new Piece(null, 0, 2));
        // |0|1|2|  |3|4|
        assertEquals(2, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(2, list.get(1).length());
        assertEquals(0, list.at(2).index());
        assertEquals(0, list.at(2).position());
        assertEquals(1, list.at(3).index());
        assertEquals(3, list.at(3).position());
        assertEquals(1, list.at(4).index());
        assertEquals(3, list.at(4).position());


        list.add(2, new Piece(null, 0, 4));
        // |0|1|2|  |3|4|  |5|6|7|8|
        assertEquals(3, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(2, list.get(1).length());
        assertEquals(4, list.get(2).length());
        assertEquals(0, list.at(0).index());
        assertEquals(0, list.at(0).position());
        assertEquals(0, list.at(2).index());
        assertEquals(0, list.at(2).position());
        assertEquals(1, list.at(3).index());
        assertEquals(3, list.at(3).position());
        assertEquals(1, list.at(4).index());
        assertEquals(3, list.at(4).position());
        assertEquals(2, list.at(5).index());
        assertEquals(5, list.at(5).position());
        assertEquals(2, list.at(8).index());
        assertEquals(5, list.at(8).position());


        list.remove(1);
        // |0|1|2|  |3|4|5|6|
        assertEquals(2, list.length());
        assertEquals(3, list.get(0).length());
        assertEquals(4, list.get(1).length());
        assertEquals(1, list.at(3).index());
        assertEquals(3, list.at(3).position());
        assertEquals(1, list.at(6).index());
        assertEquals(3, list.at(6).position());

    }


    @Test
    void testBytes() {

        var list = new CursoredList();

        Buffer buf = ReadBuffer.of("abc_def_ghi".getBytes(cs));
        list.add(0, new Piece(buf, 0, 3));
        list.add(1, new Piece(buf, 4, 3));
        list.add(2, new Piece(buf, 8, 3));

        assertArrayEquals("abcdefghi".getBytes(cs), list.bytes(0, 9).get());
        assertArrayEquals("abc".getBytes(cs), list.bytes(0, 3).get());
        assertArrayEquals("def".getBytes(cs), list.bytes(3, 6).get());
        assertArrayEquals("ghi".getBytes(cs), list.bytes(6, 9).get());
        assertArrayEquals("bcdefgh".getBytes(cs), list.bytes(1, 8).get());

        assertArrayEquals("abcdefghi".getBytes(cs), list.bytes(0, b -> b[0] == 'z').get());
        assertArrayEquals("bcdefgh".getBytes(cs), list.bytes(1, b -> b[0] == 'i').get());
        assertArrayEquals("def".getBytes(cs), list.bytes(3, b -> b[0] == 'g').get());
    }


    @Test
    void testBytesBefore() {

        var list = new CursoredList();

        Buffer buf = ReadBuffer.of("abc_def_ghi".getBytes(cs));
        list.add(0, new Piece(buf, 0, 3));
        list.add(1, new Piece(buf, 4, 3));
        list.add(2, new Piece(buf, 8, 3));

        assertArrayEquals("abcdefghi".getBytes(cs), list.bytesBefore(9, b -> b[0] == '1').get());
        assertArrayEquals("defghi".getBytes(cs), list.bytesBefore(9, b -> b[0] == 'c').get());
        assertArrayEquals("bcdefgh".getBytes(cs), list.bytesBefore(8, b -> b[0] == 'a').get());
        assertArrayEquals("def".getBytes(cs), list.bytesBefore(6, b -> b[0] == 'c').get());

    }


    @Test
    void testCount() {

        var list = new CursoredList();

        Buffer buf = ReadBuffer.of("abc_121_abc".getBytes(cs));
        list.add(0, new Piece(buf, 0, 3));
        list.add(1, new Piece(buf, 4, 3));
        list.add(2, new Piece(buf, 8, 3));

        assertEquals(0, list.count(0, 9, b -> b[0] == 'z'));
        assertEquals(2, list.count(0, 9, b -> b[0] == 'a'));
        assertEquals(2, list.count(0, 9, b -> b[0] == 'b'));
        assertEquals(2, list.count(0, 9, b -> b[0] == 'c'));

        assertEquals(2, list.count(3, 6, b -> b[0] == '1'));
        assertEquals(1, list.count(3, 6, b -> b[0] == '2'));

        assertEquals(1, list.count(1, 8, b -> b[0] == 'a'));
        assertEquals(2, list.count(1, 8, b -> b[0] == 'b'));
        assertEquals(1, list.count(1, 8, b -> b[0] == 'c'));

        assertEquals(1, list.count(1, 2, b -> b[0] == 'b'));
        assertEquals(1, list.count(6, 8, b -> b[0] == 'b'));

    }

}
