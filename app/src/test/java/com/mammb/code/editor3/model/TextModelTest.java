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

import com.mammb.code.editor3.lang.StringMetrics;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link TextModel}.
 * @author Naotsugu Kobayashi
 */
class TextModelTest {

    @Test void add() {
        var model = new TextModel();
        model.add(0, "a\nb");
        assertEquals("""
            a
            b""", model.string());
    }


    @Test void addAndTrimMaxRows() {
        var model = textModel();
        assertEquals("""
            1
            2
            3
            4
            5
            """, model.string());
    }


    @Test void delete() {
        var model = textModel();
        model.delete(0, 6);
        assertEquals("""
            4
            5
            6
            7
            8
            """, model.string());
    }


    @Test void scrollNext() {
        var model = textModel();
        model.scrollNext(1);
        assertEquals("""
            2
            3
            4
            5
            6
            """, model.string());
        assertEquals(1, model.originRowIndex());

        model.scrollNext(2);
        assertEquals("""
            4
            5
            6
            7
            8
            """, model.string());
        assertEquals(3, model.originRowIndex());

        model.scrollNext(4);
        assertEquals("""
            4
            5
            6
            7
            8
            """, model.string());
        assertEquals(3, model.originRowIndex());
    }

    @Test void scrollPrev() {
        var model = textModel();
        model.scrollNext(4);
        assertEquals("""
            4
            5
            6
            7
            8
            """, model.string());

        model.scrollPrev(1);
        assertEquals("""
            3
            4
            5
            6
            7
            """, model.string());
        assertEquals(2, model.originRowIndex());

        model.scrollPrev(1);
        assertEquals("""
            2
            3
            4
            5
            6
            """, model.string());
        assertEquals(1, model.originRowIndex());

        model.scrollPrev(1);
        assertEquals("""
            1
            2
            3
            4
            5
            """, model.string());
        assertEquals(0, model.originRowIndex());
    }


    private TextModel textModel() {
        var model = new TextModel();
        model.setupMaxRows(5);
        String text = IntStream.rangeClosed(1, 8)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining("\n", "", "\n"));
        model.add(0, text);
        return model;
    }

}
