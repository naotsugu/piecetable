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

import com.mammb.code.piecetable.text.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * The document statistics.
 * @author Naotsugu Kobayashi
 */
public interface DocumentStat {

    /**
     * Get the {@link Charset}.
     * @return the {@link Charset}
     */
    Charset charset();

    /**
     * Get the count of carriage return.
     * @return the count of carriage return
     */
    int crCount();

    /**
     * Get the count of line feed.
     * @return the count of line feed
     */
    int lfCount();

    /**
     * Get the byte order mark.
     * @return byte order mark
     */
    byte[] bom();

    /**
     * Get the estimated row terminator.
     * @return the estimated row terminator
     */
    default RowEnding rowEnding() {
        return RowEnding.estimate(crCount(), lfCount());
    }

    /**
     * Create a new {@link DocumentStat}.
     * @param path the path to be read
     * @param rowLimit the limit of row
     * @param matches the {@link CharsetMatch} used in reading the target file
     * @return a new {@link DocumentStat}.
     */
    static DocumentStat of(Path path, int rowLimit, CharsetMatch... matches) {
        return Reader.of(path, rowLimit, matches);
    }

}
