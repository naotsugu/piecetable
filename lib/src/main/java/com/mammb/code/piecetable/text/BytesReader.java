/*
 * Copyright 2022-2026 the original author or authors.
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
import com.mammb.code.piecetable.charset.Bom;
import com.mammb.code.piecetable.charset.CharsetMatches;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * A reader implementation for processing an array of bytes.
 * This class reads the given byte array, determines the character encoding,
 * and computes line ending counts while keeping track of the content's row index.
 * @author Naotsugu Kobayashi
 */
public class BytesReader implements Reader {

    /** The row index. */
    private RowIndex index;
    /** The byte order mark. */
    private byte[] bom = new byte[0];
    /** The charset read. */
    private Charset charset;
    /** The byte length read. */
    private long length = 0;
    /** The CharsetMatches. */
    private final List<CharsetMatch> matches;
    /** The count of the carriage return. */
    private int crCount = 0;
    /** The count of line feed. */
    private int lfCount = 0;

    /**
     * Constructor.
     * @param bytes the bytes to be read
     * @param matches the CharsetMatches
     */
    BytesReader(byte[] bytes, CharsetMatch... matches) {
        this.matches = List.of(matches);
        read(bytes);
    }

    @Override
    public RowIndex index() {
        return index;
    }

    @Override
    public Charset charset() {
        return (charset == null) ? StandardCharsets.UTF_8 : charset;
    }

    @Override
    public int crCount() {
        return crCount;
    }

    @Override
    public int lfCount() {
        return lfCount;
    }

    @Override
    public byte[] bom() {
        return bom;
    }

    /**
     * Read the bytes.
     * @param bytes the bytes to be reade
     */
    private void read(byte[] bytes) {

        if (length == 0) {
            // charset detection
            bom = Bom.extract(bytes);
            if (bom.length > 0) {
                charset = Bom.toCharset(bom);
                // exclude BOM
                bytes = Arrays.copyOfRange(bytes, bom.length, bytes.length);
            } else {
                charset = CharsetMatches.estimate(bytes, matches).orElse(null);
            }
        }

        length += bytes.length;

        index = RowIndex.of(charset);
        int[] crlf = index.add(bytes);
        crCount += crlf[0];
        lfCount += crlf[1];

        index.buildStCache();

    }

}
