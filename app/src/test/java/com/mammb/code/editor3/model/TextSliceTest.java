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
package com.mammb.code.editor3.model;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link TextSlice}.
 * @author Naotsugu Kobayashi
 */
class TextSliceTest {

    @Test void tailRowAndTailOffset() {
        var slice = new TextSlice(new TextSource(new ContentImpl()));
        slice.setMaxRowSize(5);
        slice.insert(0, IntStream.rangeClosed(1, 8)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining("\n", "", "\n")));
        // 1:  1$
        // 2:  2$
        // 3:  3$
        // 4:  4$
        // 5:  5$
        // 6: |
        assertEquals(6, slice.tailRow());
        assertEquals(10, slice.tailOffset());

        slice.shiftRow(1);
        // 2:  2$
        // 3:  3$
        // 4:  4$
        // 5:  5$
        // 6:  6$
        // 7: |
        assertEquals(7, slice.tailRow());
        assertEquals(12, slice.tailOffset());

        slice.delete(0, 8);
        // 2:  6$
        // 3:  7$
        // 4:  8$
        // 5: |
        assertEquals(5, slice.tailRow());
        assertEquals(8, slice.tailOffset());

    }


    @Test void insert() {

        var slice = new TextSlice(new TextSource(new ContentImpl()));
        slice.setMaxRowSize(4);

        slice.insert(0, "1"); slice.insert(1, "\n");
        slice.insert(2, "2"); slice.insert(3, "\n");
        slice.insert(4, "3"); slice.insert(5, "\n");
        slice.insert(6, "4"); slice.insert(7, "\n");
        slice.insert(8, "5"); slice.insert(9, "\n");

        assertEquals("""
            1
            2
            3
            4
            """, slice.string());
        assertEquals(5, slice.buffer().rowViewSize());
        assertEquals(6, slice.totalRowSize());
        assertTrue(slice.hasNext());
    }

    @Test void totalRowSize() {
        var slice = new TextSlice(new TextSource(new ContentImpl()));
        assertEquals(1, slice.totalRowSize());

        slice.insert(1, "a");
        assertEquals(1, slice.totalRowSize());

        slice.insert(2, "\n");
        assertEquals(2, slice.totalRowSize());

        slice.insert(3, "b");
        assertEquals(2, slice.totalRowSize());
    }
}
