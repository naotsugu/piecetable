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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link Edit}.
 * @author Naotsugu Kobayashi
 */
class EditTest {

    @Test void merge() {
        var e1 = Edit.insert(1, 2, "ab");
        var e2 = Edit.insert(1, 4, "cd");

        assertTrue(e1.canMerge(e2));
        assertFalse(e2.canMerge(e1));

        var merged = e1.merge(e2);
        assertEquals(new OffsetPoint(1, 2), merged.offsetPoint());
        assertEquals("abcd", merged.string());

    }

}

