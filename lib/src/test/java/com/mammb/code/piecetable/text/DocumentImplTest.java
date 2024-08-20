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
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.PieceTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link DocumentImpl}.
 * @author Naotsugu Kobayashi
 */
class DocumentImplTest {

    @Test
    void test() {

        var doc = DocumentImpl.of();
        assertEquals(0, doc.rows());
        assertEquals("", doc.getText(0));

        doc.insert(0, 0, "a");
        assertEquals(1, doc.rows());
        assertEquals("a", doc.getText(0));

        doc.delete(0, 0, 1);
        assertEquals(1, doc.rows());
        assertEquals("", doc.getText(0));

    }

//    @Test
//    void testEdit() {
//        var doc = DocumentImpl.of();
//        doc.insert(0, 0, "ab\ncd");
//        doc.delete(1, 1, 1);
//    }


    @Test
    void utf8(@TempDir Path tempDir) throws IOException {

        var file = tempDir.resolve("file.txt");
        Files.write(file, "a\nb\nc\n".getBytes(StandardCharsets.UTF_8));
        // |a|$|
        // |b|$|
        // |c|$|

        var doc = new DocumentImpl(PieceTable.of(file), file, Reader.of(file));
        assertEquals(4, doc.rows());

        assertEquals("b\n", doc.getText(1));
        doc.delete(1, 0, 1);
        assertEquals("\n", doc.getText(1));
        doc.insert(1, 0, "b");
        assertEquals("b\n", doc.getText(1));
    }

    @Test
    void utf8Multibyte(@TempDir Path tempDir) throws IOException {

        var file = tempDir.resolve("file.txt");
        Files.write(file, "あいう\nえお\nΩ𠀋\n".getBytes(StandardCharsets.UTF_8));
        // |あ|い|う|$|
        // |え|お||$|
        // |Ω|𠀋|$|

        var doc = new DocumentImpl(PieceTable.of(file), file, Reader.of(file));
        assertEquals(4, doc.rows());

        assertEquals("あいう\n", doc.getText(0));
        assertEquals("えお\n", doc.getText(1));
        assertEquals("Ω𠀋\n", doc.getText(2));

        // あ:2byte on UTF-16  1char  3byte on UTF-8
        doc.insert(0, 2, "アイウ");
        assertEquals("あいアイウう\n", doc.getText(0));

        doc.delete(0, 2, "アイウ");
        assertEquals("あいう\n", doc.getText(0));

        // Ω:2byte on UTF-16  1char
        // 𠀋:4byte on UTF-16 2char
        doc.insert(2, 3, "アイウ");
        assertEquals("Ω𠀋アイウ\n", doc.getText(2));

        doc.delete(2, 3, "アイウ");
        assertEquals("Ω𠀋\n", doc.getText(2));
    }



    @Test
    void utf16(@TempDir Path tempDir) throws IOException {

        var file = tempDir.resolve("file.txt");
        Files.write(file, "a\nbc\ndef\n".getBytes(StandardCharsets.UTF_16)); // contains bom

        var doc = new DocumentImpl(PieceTable.of(file), file, Reader.of(file));
        assertEquals(4, doc.rows());

        doc.insert(0, 0, "0");
        assertEquals("0a\n", doc.getText(0));

        doc.insert(1, 0, "1");
        assertEquals("1bc\n", doc.getText(1));

        doc.insert(2, 0, "2");
        assertEquals("2def\n", doc.getText(2));
    }

}
