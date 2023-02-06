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
package com.mammb.code.editor2.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * TextSource.
 * @author Naotsugu Kobayashi
 */
public class TextSource {

    /** The source charset. */
    public final Charset charset = StandardCharsets.UTF_8;

    /** The source text content. */
    private Content source;

    /** The code point index on source view. */
    private int sourceIndex;

    /**
     * Constructor.
     * @param source the code point index
     */
    public TextSource(Content source) {
        this.source = source;
        this.sourceIndex = 0;
    }

    /**
     * Get the source index.
     * @return the source index
     */
    public int sourceIndex() {
        return sourceIndex;
    }

    /**
     * Shifts source index.
     * @param deltaCodePoint delta code point
     */
    public void shiftIndex(int deltaCodePoint) {
        int newValue = sourceIndex + deltaCodePoint;
        if (newValue < 0 || newValue > source.length()) {
            throw new IndexOutOfBoundsException();
        }
        sourceIndex += deltaCodePoint;
    }

    /**
     * Shifts source index at row.
     * @param rowDelta row delta
     */
    public void shiftRow(int rowDelta) {
        if (rowDelta == 0) return;
        int count = 0;
        if (rowDelta > 0) {
            byte[] row = source.bytes(sourceIndex(), Until.lf(Math.abs(rowDelta)));
            for (byte b : row) if ((b & 0xC0) != 0x80) count++;
        } else {
            byte[] row = source.bytesBefore(sourceIndex(), Until.lf(Math.abs(rowDelta) + 1));
            for (byte b : row) if ((b & 0xC0) != 0x80) count--;
        }
        shiftIndex(count);
    }

    /**
     * Get the rows.
     * @param count the number of rows to retrieve
     * @return the before row
     */
    String rows(int count) {
        byte[] tailRow = source.bytes(sourceIndex(), Until.lf(count));
        return new String(tailRow, charset);
    }

    /**
     * Get the before row.
     * @return the before row
     */
    String beforeRow() {
        byte[] headRow = source.bytesBefore(sourceIndex(), Until.lf(2));
        return new String(headRow, charset);
    }

    /**
     * Get the after row.
     * @return the after row
     */
    String afterRow(int offsetCpCount) {
        byte[] tailRow = source.bytes(sourceIndex() + offsetCpCount, Until.lf());
        return new String(tailRow, charset);
    }

}
