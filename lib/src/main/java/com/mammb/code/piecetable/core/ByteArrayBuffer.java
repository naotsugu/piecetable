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

import java.nio.ByteBuffer;

/**
 * Appendable byte array buffer.
 * @author Naotsugu Kobayashi
 */
class ByteArrayBuffer implements AppendBuffer {

    /** The byte array. */
    private final ByteArray elements;

    /**
     * Constructor.
     * @param elements the byte array
     */
    ByteArrayBuffer(ByteArray elements) {
        this.elements = elements;
    }

    /**
     * Constructor.
     */
    ByteArrayBuffer() {
        this(ByteArray.of());
    }

    /**
     * Constructor.
     * @param bytes the byte array
     */
    ByteArrayBuffer(byte[] bytes) {
        this(ByteArray.of(bytes));
    }

    @Override
    public void append(byte[] bytes) {
        elements.add(bytes);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public byte get(long index) {
        return elements.get(Math.toIntExact(index));
    }

    @Override
    public byte[] bytes(long rawStart, long rawEnd) {
        return elements.get(Math.toIntExact(rawStart), Math.toIntExact(rawEnd));
    }

    @Override
    public long length() {
        return elements.length();
    }

    @Override
    public long read(long offset, long length, ByteBuffer buffer) {
        return elements.read(Math.toIntExact(offset), Math.toIntExact(length), buffer);
    }

}
