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

import com.mammb.code.piecetable.search.Found;
import com.mammb.code.piecetable.search.SearchSource;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link SearchContextImpl}.
 * @author Naotsugu Kobayashi
 */
class SearchContextImpl_ShiftTest {

    @Test
    void shift() throws Exception {
        SearchSource source = searchSource();
        var searchContext = new SearchContextImpl(source, Runnable::run);

        Field foundsField = SearchContextImpl.class.getDeclaredField("founds");
        foundsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Found> founds = (List<Found>) foundsField.get(searchContext);
        founds.addAll(List.of(new Found(1, 3, 3), new Found(6, 3, 3), new Found(11, 3, 3)));
        // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10| 11| 12| 13| 14| 15| 16| 17| 18| 19| 20|
        //     |===========|       |===========|       |===========|

        searchContext.insert(0, 2);
        // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10| 11| 12| 13| 14| 15| 16| 17| 18| 19| 20|
        //             |===========|       |===========|       |===========|
        assertEquals(3, founds.get(0).offset());
        assertEquals(8, founds.get(1).offset());
        assertEquals(13, founds.get(2).offset());

        searchContext.insert(8, 1);
        // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10| 11| 12| 13| 14| 15| 16| 17| 18| 19| 20|
        //             |===========|           |===========|       |===========|
        assertEquals(3, founds.get(0).offset());
        assertEquals(9, founds.get(1).offset());
        assertEquals(14, founds.get(2).offset());

        searchContext.insert(12, 1);
        // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10| 11| 12| 13| 14| 15| 16| 17| 18| 19| 20|
        //             |===========|           |===========|           |===========|
        assertEquals(3, founds.get(0).offset());
        assertEquals(9, founds.get(1).offset());
        assertEquals(15, founds.get(2).offset());

        searchContext.insert(10, 1);
        // | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10| 11| 12| 13| 14| 15| 16| 17| 18| 19| 20|
        //             |===========|                                       |===========|
        assertEquals(3, founds.get(0).offset());
        assertEquals(16, founds.get(1).offset());
        assertEquals(2, founds.size());
    }

    SearchSource searchSource() {
        return new SearchSource() {
            @Override public Charset charset() { return null; }
            @Override public long length() { return 0; }
            @Override public long serial(int row, int col) { return 0; }
            @Override public int[] pos(long offset) { return new int[0]; }
            @Override public long rowFloorOffset(long offset) { return 0; }
            @Override public long rowCeilOffset(long offset) { return 0; }
            @Override public long bufferRead(long offset, long length, ByteBuffer bb) { return 0; }
            @Override public void bufferRead(long offset, long limitLength, Function<ByteBuffer, Boolean> traverseCallback) { }
        };
    }
}
