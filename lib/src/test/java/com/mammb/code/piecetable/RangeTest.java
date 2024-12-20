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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link Range}.
 * @author Naotsugu Kobayashi
 */
class RangeTest {

    @ParameterizedTest
    @CsvSource({
    //   pos1   pos2     minPos
        "0, 0,  0, 1,    0, 0",
        "0, 1,  0, 0,    0, 0",
        "0, 0,  1, 0,    0, 0",
        "1, 0,  0, 0,    0, 0",
        "1, 3,  2, 1,    1, 3",
        "2, 1,  1, 3,    1, 3",
    })
    void min(int row1, int col1, int row2, int col2, int rowMin, int colMin) {
        var range = Range.of(row1, col1, row2, col2);
        assertEquals(Pos.of(rowMin, colMin), range.min());
    }

    @ParameterizedTest
    @CsvSource({
    //   pos1   pos2     maxPos
        "0, 0,  0, 1,    0, 1",
        "0, 1,  0, 0,    0, 1",
        "0, 0,  1, 0,    1, 0",
        "1, 0,  0, 0,    1, 0",
        "1, 3,  2, 1,    2, 1",
        "2, 1,  1, 3,    2, 1",
    })
    void max(int row1, int col1, int row2, int col2, int rowMax, int colMax) {
        var range = Range.of(row1, col1, row2, col2);
        assertEquals(Pos.of(rowMax, colMax), range.max());
    }

}
