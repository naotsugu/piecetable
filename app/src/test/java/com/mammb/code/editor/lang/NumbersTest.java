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
package com.mammb.code.editor.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of {@link Numbers}.
 * @author Naotsugu Kobayashi
 */
class NumbersTest {

    @Test void isJavaNumber() {
        assertTrue(Numbers.isJavaNumber("1"));
        assertTrue(Numbers.isJavaNumber("-2"));
        assertTrue(Numbers.isJavaNumber("1_000"));
        assertTrue(Numbers.isJavaNumber("0x5_2"));
        assertTrue(Numbers.isJavaNumber("1234L"));
        assertTrue(Numbers.isJavaNumber("3.14_15F"));
        assertTrue(Numbers.isJavaNumber("2.712e-4"));
    }


    @Test void toCharArrayStripedUnderscore() {
        assertArrayEquals("_100".toCharArray(), Numbers.toCharArrayStripedUnderscore("_100"));
        assertArrayEquals("100_".toCharArray(), Numbers.toCharArrayStripedUnderscore("100_"));
        assertArrayEquals("100".toCharArray(), Numbers.toCharArrayStripedUnderscore("1_00"));
        assertArrayEquals("100".toCharArray(), Numbers.toCharArrayStripedUnderscore("1__00"));
        assertArrayEquals("1._00".toCharArray(), Numbers.toCharArrayStripedUnderscore("1._00"));
        assertArrayEquals("1_.00".toCharArray(), Numbers.toCharArrayStripedUnderscore("1_.00"));
    }

}
