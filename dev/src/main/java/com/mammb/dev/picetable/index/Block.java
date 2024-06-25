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
package com.mammb.dev.picetable.index;

public class Block {
    static final int CAPACITY = 100;
    private int length = 0;
    private long offset = 0;
    private int[] lines = new int[CAPACITY];

    Block(long offset) {
        this.offset = offset;
    }

    void put(int index, long pos) {
        lines[index] = Math.toIntExact(pos - offset);
        length++;
    }

    long get(int index) {
        return offset + lines[index];
    }

    void shift(int delta) {
        offset += delta;
    }

}
