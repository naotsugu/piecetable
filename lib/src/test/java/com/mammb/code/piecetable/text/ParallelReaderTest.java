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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link ParallelReader}.
 * @author Naotsugu Kobayashi
 */
class ParallelReaderTest {

    @TempDir
    private static Path dir;

    @Test
    void empty() {
        var target = new ParallelReader(textFile(""), null, CharsetMatches.defaults());
        assertEquals(StandardCharsets.UTF_8, target.charset());
        assertEquals(0, target.crCount());
        assertEquals(0, target.lfCount());
        assertEquals(0, target.bom().length);
        assertEquals(1, target.index().rowSize());
        assertEquals(0, target.index().rowLengths()[0]);
    }

    @Test
    void singleLine() {
        var target = new ParallelReader(textFile("abc"), null, CharsetMatches.defaults());
        assertEquals(StandardCharsets.UTF_8, target.charset());
        assertEquals(0, target.crCount());
        assertEquals(0, target.lfCount());
        assertEquals(0, target.bom().length);
        assertEquals(1, target.index().rowSize());
        assertEquals(3, target.index().rowLengths()[0]);
    }

    @Test
    void multiLine() {
        var target = new ParallelReader(textFile("abc\nde"), null, CharsetMatches.defaults());
        assertEquals(StandardCharsets.UTF_8, target.charset());
        assertEquals(0, target.crCount());
        assertEquals(1, target.lfCount());
        assertEquals(0, target.bom().length);
        assertEquals(2, target.index().rowSize());
        assertEquals(4, target.index().rowLengths()[0]);
        assertEquals(2, target.index().rowLengths()[1]);
    }


    private Path textFile(String text) {
        Path path = dir.resolve(UUID.randomUUID() + ".txt");
        try (var writer = Files.newBufferedWriter(path)) {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

}
