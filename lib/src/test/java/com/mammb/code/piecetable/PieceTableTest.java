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
package com.mammb.code.piecetable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link PieceTable}.
 * @author Naotsugu Kobayashi
 */
class PieceTableTest {

    @Test
    void test(@TempDir Path tempDir) throws IOException {

        var pt = PieceTable.of();
        pt.insert(0, "ac".getBytes());
        pt.insert(1, "bc".getBytes());
        pt.delete(3, 1);

        var bytes = pt.get(0, 3);
        assertEquals("abc", new String(bytes));

        var path = tempDir.resolve("test.txt");
        pt.save(tempDir.resolve("test.txt"));
        assertEquals("abc", Files.readString(path));

    }

    @Test
    void test() {
        var pt = PieceTable.of();
        pt.insert(0, "a large text".getBytes());
        pt.insert(8, "span of ".getBytes());
        pt.delete(1, 6);

        var bytes = pt.get(0, (int) pt.length());
        assertEquals("a span of text", new String(bytes));
    }

    @Test
    void readAll() {
        var pt = PieceTable.of();
        pt.insert(0, "0123456789".getBytes());

        pt.read(0, bb -> {
            bb.flip();
            byte[] bytes = new byte[10];
            bb.get(bytes);
            bb.compact();
            assertArrayEquals("0123456789".getBytes(), bytes);
            return true;
        });
    }

    @Test
    void readMiddle() {
        var text = "012abc345";
        var pt = PieceTable.of();
        pt.insert(pt.length(), text.substring(0, 3).getBytes());
        pt.insert(pt.length(), text.substring(3, 6).getBytes());
        pt.insert(pt.length(), text.substring(6, 9).getBytes());
        assertEquals(text, new String(pt.get(0, (int) pt.length())));

        StringBuilder sb = new StringBuilder();
        pt.read(0, bb -> {
            bb.flip();
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            sb.append(new String(bytes));
            bb.compact();
            return true;
        });
        assertEquals(text, sb.toString());

        sb.setLength(0);
        pt.read(2, bb -> {
            bb.flip();
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            sb.append(new String(bytes));
            bb.compact();
            return true;
        });
        assertEquals(text.substring(2), sb.toString());
    }

}
