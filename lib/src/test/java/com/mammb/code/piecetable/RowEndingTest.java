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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test of {@link RowEnding}.
 * @author Naotsugu Kobayashi
 */
class RowEndingTest {

    @Test
    void unifyLf() {
        assertEquals("", RowEnding.unifyLf(""));
        assertEquals("a", RowEnding.unifyLf("a"));
        assertEquals("abc", RowEnding.unifyLf("abc"));
        assertEquals("\n", RowEnding.unifyLf("\n"));
        assertEquals("\n", RowEnding.unifyLf("\r").toString());
        assertEquals("\n", RowEnding.unifyLf("\r\n").toString());
        assertEquals("\n\n", RowEnding.unifyLf("\r\n\r").toString());
        assertEquals("\n\n", RowEnding.unifyLf("\n\n"));
        assertEquals("\n\n", RowEnding.unifyLf("\r\r").toString());
        assertEquals("\n\n", RowEnding.unifyLf("\r\n\r\n").toString());
        assertEquals("abc\n", RowEnding.unifyLf("abc\n"));
        assertEquals("abc\n", RowEnding.unifyLf("abc\r").toString());
        assertEquals("abc\n", RowEnding.unifyLf("abc\r\n").toString());
        assertEquals("abc\ndef", RowEnding.unifyLf("abc\ndef"));
        assertEquals("abc\ndef", RowEnding.unifyLf("abc\rdef").toString());
        assertEquals("abc\ndef", RowEnding.unifyLf("abc\r\ndef").toString());
        assertEquals("abc\n\ndef", RowEnding.unifyLf("abc\r\r\ndef").toString());
        assertEquals("abc\ndef\n", RowEnding.unifyLf("abc\ndef\n"));
        assertEquals("abc\ndef\n", RowEnding.unifyLf("abc\rdef\r\n").toString());
        assertEquals("abc\ndef\n", RowEnding.unifyLf("abc\r\ndef\r").toString());
        assertEquals("abc\n\ndef\n\n", RowEnding.unifyLf("abc\r\r\ndef\r\n\r").toString());
    }

    @Test
    void unifyCr() {
        assertEquals("", RowEnding.unifyCr(""));
        assertEquals("a", RowEnding.unifyCr("a"));
        assertEquals("abc", RowEnding.unifyCr("abc"));
        assertEquals("\r", RowEnding.unifyCr("\n").toString());
        assertEquals("\r", RowEnding.unifyCr("\r"));
        assertEquals("\r", RowEnding.unifyCr("\r\n").toString());
        assertEquals("\r\r", RowEnding.unifyCr("\r\n\r").toString());
        assertEquals("\r\r", RowEnding.unifyCr("\n\n").toString());
        assertEquals("\r\r", RowEnding.unifyCr("\r\r"));
        assertEquals("\r\r", RowEnding.unifyCr("\r\n\r\n").toString());
        assertEquals("abc\r", RowEnding.unifyCr("abc\n").toString());
        assertEquals("abc\r", RowEnding.unifyCr("abc\r"));
        assertEquals("abc\r", RowEnding.unifyCr("abc\r\n").toString());
        assertEquals("abc\rdef", RowEnding.unifyCr("abc\ndef").toString());
        assertEquals("abc\rdef", RowEnding.unifyCr("abc\rdef"));
        assertEquals("abc\rdef", RowEnding.unifyCr("abc\r\ndef").toString());
        assertEquals("abc\r\rdef", RowEnding.unifyCr("abc\r\r\ndef").toString());
        assertEquals("abc\rdef\r", RowEnding.unifyCr("abc\ndef\n").toString());
        assertEquals("abc\rdef\r", RowEnding.unifyCr("abc\rdef\r\n").toString());
        assertEquals("abc\rdef\r", RowEnding.unifyCr("abc\r\ndef\r").toString());
        assertEquals("abc\r\rdef\r\r", RowEnding.unifyCr("abc\r\r\ndef\r\n\r").toString());
    }

    @Test
    void unifyCrLf() {
        assertEquals("", RowEnding.unifyCrLf(""));
        assertEquals("a", RowEnding.unifyCrLf("a"));
        assertEquals("abc", RowEnding.unifyCrLf("abc"));
        assertEquals("\r\n", RowEnding.unifyCrLf("\n").toString());
        assertEquals("\r\n", RowEnding.unifyCrLf("\r").toString());
        assertEquals("\r\n", RowEnding.unifyCrLf("\r\n"));
        assertEquals("\r\n\r\n", RowEnding.unifyCrLf("\r\n\r").toString());
        assertEquals("\r\n\r\n", RowEnding.unifyCrLf("\n\n").toString());
        assertEquals("\r\n\r\n", RowEnding.unifyCrLf("\r\r").toString());
        assertEquals("\r\n\r\n", RowEnding.unifyCrLf("\r\n\r\n"));
        assertEquals("abc\r\n", RowEnding.unifyCrLf("abc\n").toString());
        assertEquals("abc\r\n", RowEnding.unifyCrLf("abc\r").toString());
        assertEquals("abc\r\n", RowEnding.unifyCrLf("abc\r\n"));
        assertEquals("abc\r\ndef", RowEnding.unifyCrLf("abc\ndef").toString());
        assertEquals("abc\r\ndef", RowEnding.unifyCrLf("abc\rdef").toString());
        assertEquals("abc\r\ndef", RowEnding.unifyCrLf("abc\r\ndef"));
        assertEquals("abc\r\n\r\ndef", RowEnding.unifyCrLf("abc\r\r\ndef").toString());
        assertEquals("abc\r\ndef\r\n", RowEnding.unifyCrLf("abc\ndef\n").toString());
        assertEquals("abc\r\ndef\r\n", RowEnding.unifyCrLf("abc\rdef\r\n").toString());
        assertEquals("abc\r\ndef\r\n", RowEnding.unifyCrLf("abc\r\ndef\r").toString());
        assertEquals("abc\r\n\r\ndef\r\n\r\n", RowEnding.unifyCrLf("abc\r\r\ndef\r\n\r").toString());
    }

}
