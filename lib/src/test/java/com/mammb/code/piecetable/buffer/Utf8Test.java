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
package com.mammb.code.piecetable.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test of {@link Utf8}.
 * @author Naotsugu Kobayashi
 */
class Utf8Test {

    @Test
    void followsCount() {
        assertEquals(1, Utf8.followsCount("A".getBytes(Utf8.charset())[0]));
        assertEquals(2, Utf8.followsCount("Ω".getBytes(Utf8.charset())[0]));
        assertEquals(3, Utf8.followsCount("あ".getBytes(Utf8.charset())[0]));
        assertEquals(3, Utf8.followsCount(Utf8.bom()[0]));
        assertEquals(4, Utf8.followsCount("𠀋".getBytes(Utf8.charset())[0]));
    }

}
