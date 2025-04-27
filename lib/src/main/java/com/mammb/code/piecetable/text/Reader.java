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
package com.mammb.code.piecetable.text;

import com.mammb.code.piecetable.CharsetMatch;
import com.mammb.code.piecetable.DocumentStat;
import com.mammb.code.piecetable.Segment;
import com.mammb.code.piecetable.charset.CharsetMatches;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * The reader.
 * @author Naotsugu Kobayashi
 */
public interface Reader extends DocumentStat {

    /**
     * Get the {@link RowIndex}.
     * @return the {@link RowIndex}
     */
    RowIndex index();

    @Override
    Charset charset();

    @Override
    int crCount();

    @Override
    int lfCount();

    @Override
    byte[] bom();

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @return a new {@link Reader}.
     */
    static Reader of(Path path) {
        return new SeqReader(path, -1, null, CharsetMatches.defaults());
    }

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @param listener the progress listener
     * @return a new {@link Reader}.
     */
    static Reader of(Path path, Consumer<Segment> listener) {
        try {
            return Files.size(path) >= ParallelReader.CHUNK_SIZE
                ? new ParallelReader(path, listener, CharsetMatches.defaults())
                : new SeqReader(path, -1, listener, CharsetMatches.defaults());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @param charset the character set of the file to be read
     * @return a new {@link Reader}.
     */
    static Reader of(Path path, Charset charset) {
        return new SeqReader(path, -1, null, CharsetMatch.of(charset));
    }

    /**
     * Create a new {@link Reader}.
     * @param path the path to be read
     * @param rowLimit the limit of row
     * @param matches the {@link CharsetMatch} used in reading the target file
     * @return a new {@link Reader}.
     */
    static Reader of(Path path, int rowLimit, CharsetMatch... matches) {
        return new SeqReader(path, rowLimit, null,
            (matches == null || matches.length == 0) ? CharsetMatches.defaults() : matches);
    }

}
