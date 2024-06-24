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

public class Entry {
    int pos = 0, ch = 0, line = 0;

    public Entry() {
    }

    public Entry(int pos, int ch, int line) {
        this.pos = pos;
        this.ch = ch;
        this.line = line;
    }

    void plus(Entry entry) {
        pos += entry.pos;
        ch += entry.ch;
        line += entry.line;
    }

    void clear() {
        pos = ch = line = 0;
    }

    boolean isEmpty() {
        return pos == 0 && ch == 0 && line == 0;
    }

    Entry plusOf(Entry entry) {
        return new Entry(pos + entry.pos, ch + entry.ch, line + entry.line);
    }

}
