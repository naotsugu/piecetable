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
    void utf16(@TempDir Path tempDir) throws IOException {

        var file = tempDir.resolve("file.txt");
        Files.write(file, "a\nbc\ndef\n".getBytes(StandardCharsets.UTF_16));

        var doc = new DocumentImpl(PieceTable.of(file), file);

        doc.insert(0, 0, "0");
        assertEquals("0a\n", doc.getText(0));

        doc.insert(1, 0, "1");
        assertEquals("1bc\n", doc.getText(1));

        doc.insert(2, 0, "2");
        assertEquals("2def\n", doc.getText(2));
    }
}
