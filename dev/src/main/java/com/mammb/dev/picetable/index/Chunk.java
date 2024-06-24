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

public class Chunk {
    final Entry offset = new Entry();
    final List<Entry> entries = new ArrayList<>();

    void plusOffset(Entry entry) {
        offset.plus(entry);
    }

    Entry head() {
        return entries.get(0).plusOf(offset);
    }

    boolean containsLine(long line) {
        return entries.get(0).line + offset.line <= line &&
            line <= entries.get(entries.size() - 1).line + offset.line;
    }

    Entry lineAt(long line) {
        if (offset.isEmpty()) {
            for (Entry entry : entries) {
                if (entry.line == line) {
                    return entry;
                }
            }
        } else {
            Entry ret = null;
            for (Entry entry : entries) {
                entry.plus(offset);
                if (entry.line == line) {
                    ret = entry;
                }
            }
            offset.clear();
            return ret;
        }
        return null;
    }

    void flat() {
        for (Entry entry : entries) {
            entry.plus(offset);
        }
        offset.clear();
    }

}
