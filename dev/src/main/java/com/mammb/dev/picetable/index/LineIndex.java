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

import java.util.ArrayList;
import java.util.List;

public class LineIndex {
    private final List<Block> blocks = new ArrayList<>();

    public LineIndex() {
        blocks.add(new Block(0));
    }

    void put(int line, long pos) {
        int blockIndex = line / Block.CAPACITY;
        if (blockIndex > blocks.size() - 1) {
            var block = new Block(pos);
            block.put(0, pos);
            blocks.add(block);
        }
        blocks.get(blockIndex)
            .put(line % Block.CAPACITY, pos);
    }

    long get(int line) {
        int blockIndex = line / Block.CAPACITY;
        return blocks.get(blockIndex).get(line % Block.CAPACITY);
    }

}
