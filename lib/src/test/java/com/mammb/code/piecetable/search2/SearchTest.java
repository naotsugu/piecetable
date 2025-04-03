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
package com.mammb.code.piecetable.search2;

import com.mammb.code.piecetable.PieceTable;
import com.mammb.code.piecetable.text.DocumentImpl;
import com.mammb.code.piecetable.text.Reader;
import com.mammb.code.piecetable.text.RowIndex;
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
            return true;
        });

        Search.caseInsensitiveOf(source).search("BCD", 0, 0, fic -> {
            var founds = fic.founds();
            assertEquals(1, founds.size());
            assertEquals(10, founds.getFirst().offset());
            assertEquals(3, founds.getFirst().len());
            return true;
        });

        Search.regexOf(source).search("b.?d", 0, 0, fic -> {
            var founds = fic.founds();
            assertEquals(1, founds.size());
            assertEquals(10, founds.getFirst().offset());
            assertEquals(3, founds.getFirst().len());
            return true;
        });

    }

    private SerialSource source(Path path) {
        var pt = PieceTable.of(path);
        var index = Reader.of(path).index();
        return new SerialSource() {
            @Override public Charset charset() {
                return StandardCharsets.UTF_8;
            }
            @Override public long length() {
                return pt.length();
            }
            @Override public long serial(int row, int col) {
                return index.serial(row, col);
            }
            @Override public long rowFloorSerial(long serial) {
                return index.rowFloorSerial(serial);
            }
            @Override public long rowCeilSerial(long serial) {
                return index.rowCeilSerial(serial);
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
