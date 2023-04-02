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
package com.mammb.code.piecetable.buffer;

/**
 * Appendable buffer.
 * @author Naotsugu Kobayashi
 */
public interface AppendBuffer extends Buffer {

    /**
     * Add a char sequence to this buffer.
     * @param cs char sequence to be appended
     */
    void append(CharSequence cs);

    /**
     * Add the other buffer to this buffer.
     * @param buffer buffer to be appended
     */
    void append(Buffer buffer);

    /**
     * Add the byte array to this buffer.
     * @param bytes the byte array to be appended
     */
    void append(byte[] bytes);

    /**
     * Clear the buffer.
     */
    void clear();

}
