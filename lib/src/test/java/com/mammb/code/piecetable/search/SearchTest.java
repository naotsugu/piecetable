/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.search;

import com.mammb.code.piecetable.PieceTable;
import com.mammb.code.piecetable.text.Reader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link Search}.
 * @author Naotsugu Kobayashi
 */
class SearchTest {

    @TempDir
    private static Path dir;

    @Test
    void searchOne() {

        var source = source(content("a".repeat(10) + "bcd" + "e".repeat(10)));

        Search.of(source).search("bcd", 0, 0, fic -> {
            var founds = fic.founds();
            assertEquals(1, founds.size());
            assertEquals(10, founds.getFirst().offset());
            assertEquals(3, founds.getFirst().len());
        });

        Search.caseInsensitiveOf(source).search("BCD", 0, 0, fic -> {
            var founds = fic.founds();
            assertEquals(1, founds.size());
            assertEquals(10, founds.getFirst().offset());
            assertEquals(3, founds.getFirst().len());
        });

        Search.regexOf(source).search("b.?d", 0, 0, fic -> {
            var founds = fic.founds();
            assertEquals(1, founds.size());
            assertEquals(10, founds.getFirst().offset());
            assertEquals(3, founds.getFirst().len());
        });

    }

    @Test
    void searchRange() {
        var source = source(content(
            "a".repeat(10) + "bcd" + "e".repeat(10) + "\n" +
            "f".repeat(10) + "bcd"));
        var founds = Search.of(source).search("bc", 0, 0, 1, 13);
        assertEquals(2, founds.size());
        assertEquals(10, founds.getFirst().offset());
        assertEquals(2, founds.getFirst().len());
        assertEquals(34, founds.get(1).offset());
        assertEquals(2, founds.get(1).len());
    }

    private SearchSource source(Path path) {
        var pt = PieceTable.of(path);
        var index = Reader.of(path).index();
        return new SearchSource() {
            @Override public Charset charset() {
                return StandardCharsets.UTF_8;
            }
            @Override public long length() {
                return pt.length();
            }
            @Override public long offset(int row, int col) {
                return index.offset(row, col);
            }
            @Override public int[] pos(long offset) {
                return index.pos(offset);
            }
            @Override public long rowFloorOffset(long offset) {
                return index.rowFloorOffset(offset);
            }
            @Override public long rowCeilOffset(long offset) {
                return index.rowCeilOffset(offset);
            }
            @Override public long bufferRead(long offset, long length, ByteBuffer bb) {
                return pt.read(offset, length, bb);
            }
            @Override public void bufferRead(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback) {
                pt.read(offset, limitLength, traverseCallback);
            }
        };
    }

    private Path content(String text) {
        Path path = dir.resolve(UUID.randomUUID() + ".txt");
        try (var writer = Files.newBufferedWriter(path)) {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

}
