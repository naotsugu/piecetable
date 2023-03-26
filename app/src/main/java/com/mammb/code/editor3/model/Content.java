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

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Content.
 * @author Naotsugu Kobayashi
 */
public interface Content extends EditListener {

    /**
     * Get the content path.
     * @return the content path
     */
    Path path();

    /**
     * Get the content length.
     * length is a code point count.
     * @return the content length
     */
    int length();

    /**
     * Get the total row size.
     * @return the total row size
     */
    int rowSize();

    /**
     * Get the bytes.
     * <pre>
     *     | a | b | c | d |
     *       L startPos
     *                   L  until [d] appears [a, b, c]
     * </pre>
     * @param startPos start position(code point index), inclusive
     * @param until the until predicate, exclusive
     * @return the bytes
     */
    byte[] bytes(int startPos, Predicate<byte[]> until);

    /**
     * Get the code point count.
     * @param startPos start position(code point index), inclusive
     * @param until the until predicate, exclusive
     * @return the code point count
     */
    int count(int startPos, Predicate<byte[]> until);

    /**
     * Get the bytes.
     * <pre>
     *     | a | b | c | d | a |
     *                       L startPos
     *      L  until [a] appears [a, b, c]
     * </pre>
     * @param startPos start position(code point index), exclusive
     * @param until the until predicate, exclusive
     * @return the bytes
     */
    byte[] bytesBefore(int startPos, Predicate<byte[]> until);

    void save();

    void saveAs(Path path);

}
