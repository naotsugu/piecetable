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

import com.mammb.code.piecetable.Document;
import com.mammb.code.piecetable.Findable.Found;
import com.mammb.code.piecetable.Findable.FoundListener;
import com.mammb.code.piecetable.Pos;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * The fast naive search.
 * Bypass String instantiation by comparing them as a byte array.
 * @author Naotsugu Kobayashi
 */
public class FastNaiveSearch implements Search {

    /** The source document. */
    private final Document doc;

    /**
     * Constructor.
     * @param doc the source document
     */
    public FastNaiveSearch(Document doc) {
        this.doc = doc;
    }

    @Override
    public void search(CharSequence cs, int fromRow, int fromCol, FoundListener listener) {

        if (cs == null || cs.isEmpty()) return;

        byte[] pattern = cs.toString().getBytes(doc.charset());
        long serial = doc.serial(fromRow, fromCol);
        long[] n = new long[] { 0 };
        byte first = pattern[0];

        Function<ByteBuffer, Boolean> traverse = bb -> {
            bb.flip();
            while (bb.remaining() >= pattern.length) {
                int matchLen;
                n[0]++;
                if (first != bb.get()) continue;
                for (matchLen = 1; matchLen < pattern.length; matchLen++) {
                    n[0]++;
                    if (pattern[matchLen] != bb.get()) break;
                }
                if (matchLen == pattern.length) {
                    Pos pos = doc.pos(serial + n[0] - matchLen);
                    var found = new Found(pos.row(), pos.col(), cs.length());
                    if (!listener.apply(found)) {
                        return false;
                    }
                }
            }
            bb.compact();
            return true;
        };

        doc.bufferRead(serial, -1, traverse);
    }

    @Override
    public void searchDesc(CharSequence pattern, int fromRow, int fromCol, FoundListener listener) {
        throw new UnsupportedOperationException();
    }

}
