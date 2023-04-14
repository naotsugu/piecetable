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
 * Test of {@link TextModel}.
 * @author Naotsugu Kobayashi
 */
class TextModelTest {

    @Test void add() {
        var model = new TextModel();
        model.add(0, "a\nb");
        assertEquals("""
            a
            b""", model.stringSlice());
    }


    @Test void addAndTrimMaxRows() {
        var model = textModel();
        assertEquals("""
            1
            2
            3
            4
            5
            """, model.stringSlice());
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
            """, model.stringSlice());
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
            """, model.stringSlice());
        assertEquals(1, model.originRowIndex());

        model.scrollNext(2);
        assertEquals("""
            4
            5
            6
            7
            8
            """, model.stringSlice());
        assertEquals(3, model.originRowIndex());

        model.scrollNext(4);
        assertEquals("""
            4
            5
            6
            7
            8
            """, model.stringSlice());
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
            """, model.stringSlice());

        model.scrollPrev(1);
        assertEquals("""
            3
            4
            5
            6
            7
            """, model.stringSlice());
        assertEquals(2, model.originRowIndex());

        model.scrollPrev(1);
        assertEquals("""
            2
            3
            4
            5
            6
            """, model.stringSlice());
        assertEquals(1, model.originRowIndex());

        model.scrollPrev(3);
        assertEquals("""
            1
            2
            3
            4
            5
            """, model.stringSlice());
        assertEquals(0, model.originRowIndex());

    }


    @Test void edit() {
        var model = new TextModel();
        model.add(0, "a");
        model.add(1, "b");
        model.add(2, "c");
        model.delete(2, 1);
        assertEquals("ab", model.stringSlice());
        assertEquals("ab", model.substring(0, 3));
    }

    @Test void edit2() {
        var model = new TextModel();
        model.setupMaxRows(3);
        model.add(0, "1\n");
        model.add(2, "2\n");
        model.add(4, "3\n");
        model.add(6, "4\n");
        model.add(8, "5\n");
        assertEquals("1\n2\n3\n", model.stringSlice());
        assertEquals("1\n2\n3\n4\n5\n", model.substring(0, 10));
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
