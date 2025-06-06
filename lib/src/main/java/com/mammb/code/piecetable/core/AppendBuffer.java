/*
 * Copyright 2022-2025 the original author or authors.
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
package com.mammb.code.piecetable.core;

/**
 * Represents a buffer that allows appending additional byte arrays to it.
 * Provides functionality to add data, clear the buffer, and create new instances
 * of the buffer.
 * @author Naotsugu Kobayashi
 */
public interface AppendBuffer extends Buffer {

    /**
     * Add the byte array to this buffer.
     * @param bytes the byte array to be appended
     */
    void append(byte[] bytes);

    /**
     * Clear the buffer.
     */
    void clear();

    /**
     * Create a new appendable buffer.
     * @return a new appendable buffer
     */
    static AppendBuffer of() {
        return new ByteArrayBuffer();
    }

    /**
     * Create a new appendable buffer.
     * @param bytes the byte array
     * @return a new appendable buffer
     */
    static AppendBuffer of(byte[] bytes) {
        return new ByteArrayBuffer(bytes);
    }

}

